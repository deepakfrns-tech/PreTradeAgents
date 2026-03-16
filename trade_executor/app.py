"""Trade Executor Agent — Flask app on port 8082.

Scheduled to trigger paper trades at 9:15 AM IST.
Also provides manual trigger and trade query endpoints.
"""

import os
from datetime import date

from flask import Flask, jsonify
from apscheduler.schedulers.background import BackgroundScheduler

from shared.database import SessionLocal
from shared.models import TradeDecision, PaperTrade
from shared.time_utils import today_ist, now_ist, is_market_open, IST

app = Flask(__name__)

# Configuration
MAX_POSITIONS = int(os.environ.get("MAX_POSITIONS", "5"))
MAX_LOSS_PER_TRADE = float(os.environ.get("MAX_LOSS_PER_TRADE", "2000"))
MAX_DAILY_LOSS = float(os.environ.get("MAX_DAILY_LOSS", "10000"))
EOD_EXIT_TIME = os.environ.get("EOD_EXIT_TIME", "15:15")
TRAILING_STOP_PERCENT = float(os.environ.get("TRAILING_STOP_PERCENT", "30"))


def execute_market_open_trades():
    """Main execution logic — reads approved decisions, creates paper trades."""
    today = today_ist()
    print(f"=== Market Open Trade Execution triggered for {today} ===")

    db = SessionLocal()
    try:
        approved = (
            db.query(TradeDecision)
            .filter(TradeDecision.trade_date == today, TradeDecision.decision == "APPROVED")
            .all()
        )

        if not approved:
            print("No approved trades for today. Skipping.")
            return

        print(f"Found {len(approved)} approved trade decisions")

        open_count = (
            db.query(PaperTrade)
            .filter(PaperTrade.trade_date == today, PaperTrade.status == "OPEN")
            .count()
        )

        opened = 0
        for decision in approved:
            if open_count >= MAX_POSITIONS:
                print(f"Max positions ({MAX_POSITIONS}) reached. Skipping remaining.")
                break

            existing = (
                db.query(PaperTrade)
                .filter(PaperTrade.trade_date == today, PaperTrade.signal_id == decision.signal_id)
                .first()
            )
            if existing:
                continue

            entry_price = decision.final_strike
            if entry_price is None and decision.signal:
                entry_price = decision.signal.estimated_premium

            now = now_ist().replace(tzinfo=None)
            trade = PaperTrade(
                trade_date=today,
                symbol=decision.symbol,
                signal_id=decision.signal_id,
                direction=decision.final_direction,
                strike_price=decision.final_strike,
                entry_price=entry_price,
                entry_time=now,
                lot_size=decision.final_lot_size,
                entry_reasoning=decision.decision_reason,
                status="OPEN",
                max_profit=0.0,
                max_drawdown=0.0,
                created_at=now,
            )
            db.add(trade)
            open_count += 1
            opened += 1
            print(f"  Opened: {trade.direction} {trade.symbol} @ strike {trade.strike_price} (lot: {trade.lot_size})")

        db.commit()
        print(f"=== Opened {opened} paper trades for {today} ===")
    except Exception as e:
        db.rollback()
        print(f"Error executing trades: {e}")
    finally:
        db.close()


def monitor_positions():
    """Check open positions for EOD exit."""
    if not is_market_open():
        return

    today = today_ist()
    now = now_ist()

    eod_h, eod_m = map(int, EOD_EXIT_TIME.split(":"))
    force_exit = now.hour > eod_h or (now.hour == eod_h and now.minute >= eod_m)

    if not force_exit:
        return

    db = SessionLocal()
    try:
        open_trades = (
            db.query(PaperTrade)
            .filter(PaperTrade.trade_date == today, PaperTrade.status == "OPEN")
            .all()
        )
        for trade in open_trades:
            trade.exit_time = now.replace(tzinfo=None)
            trade.exit_reason = "EOD_EXIT"
            trade.exit_reasoning = f"End of day forced exit at {EOD_EXIT_TIME}"
            trade.status = "CLOSED"
            print(f"  EOD exit: {trade.symbol} {trade.direction}")

        db.commit()
    except Exception as e:
        db.rollback()
        print(f"Error monitoring positions: {e}")
    finally:
        db.close()


# --- Scheduler ---
scheduler = BackgroundScheduler(timezone=IST)
scheduler.add_job(execute_market_open_trades, "cron", day_of_week="mon-fri", hour=9, minute=15, id="market_open")
scheduler.add_job(monitor_positions, "interval", seconds=30, id="monitor")


# --- REST Endpoints ---
@app.route("/api/executor/health")
def health():
    return jsonify({"status": "UP", "agent": "trade-executor", "time": str(now_ist())})


@app.route("/api/executor/trigger", methods=["POST"])
def manual_trigger():
    execute_market_open_trades()
    return jsonify({"status": "triggered", "time": str(now_ist())})


@app.route("/api/executor/trades/<trade_date>")
def get_trades(trade_date):
    d = date.fromisoformat(trade_date)
    db = SessionLocal()
    try:
        trades = db.query(PaperTrade).filter(PaperTrade.trade_date == d).all()
        return jsonify([t.to_dict() for t in trades])
    finally:
        db.close()


@app.route("/api/executor/trades/<trade_date>/open")
def get_open_trades(trade_date):
    d = date.fromisoformat(trade_date)
    db = SessionLocal()
    try:
        trades = (
            db.query(PaperTrade)
            .filter(PaperTrade.trade_date == d, PaperTrade.status == "OPEN")
            .all()
        )
        return jsonify([t.to_dict() for t in trades])
    finally:
        db.close()


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8082))
    scheduler.start()
    print(f"Starting Trade Executor on port {port}")
    print(f"  Scheduled: 9:15 AM IST (Mon-Fri) for trade execution")
    print(f"  Monitoring: every 30s for EOD exit at {EOD_EXIT_TIME}")
    try:
        app.run(host="0.0.0.0", port=port, debug=False)
    finally:
        scheduler.shutdown()
