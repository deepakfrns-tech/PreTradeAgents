"""Learning Summary Agent — Flask app on port 8083.

Generates daily summaries and mines strategy patterns from trade history.
"""

import json
import os
from datetime import date, time as dt_time, timedelta

from flask import Flask, request, jsonify

from shared.database import SessionLocal
from shared.models import PaperTrade, DailySummary, StrategyLearning, StockAnalysis, TradeDecision
from shared.time_utils import today_ist, now_ist
from shared.formatters import format_pnl

app = Flask(__name__)

# Configuration
MIN_CONFIDENCE = float(os.environ.get("MIN_CONFIDENCE", "0.7"))
LOOKBACK_DAYS = int(os.environ.get("LOOKBACK_DAYS", "30"))
PATTERN_MIN_OCCURRENCES = int(os.environ.get("PATTERN_MIN_OCCURRENCES", "3"))


def generate_daily_summary(trade_date: date) -> dict | None:
    """Aggregate trades into a DailySummary."""
    print(f"=== Generating daily summary for {trade_date} ===")

    db = SessionLocal()
    try:
        trades = db.query(PaperTrade).filter(PaperTrade.trade_date == trade_date).all()
        signals = db.query(StockAnalysis).filter(StockAnalysis.trade_date == trade_date).all()
        decisions = db.query(TradeDecision).filter(TradeDecision.trade_date == trade_date).all()

        if not trades and not signals:
            print(f"No trades or signals for {trade_date}. Skipping.")
            return None

        closed = [t for t in trades if t.status == "CLOSED"]
        total = len(closed)
        winners = [t for t in closed if t.pnl_amount and t.pnl_amount > 0]
        losers = [t for t in closed if t.pnl_amount and t.pnl_amount < 0]

        total_pnl = sum(t.pnl_amount for t in closed if t.pnl_amount) or 0
        win_rate = (len(winners) / total * 100) if total > 0 else 0

        total_profit = sum(t.pnl_amount for t in winners if t.pnl_amount) or 0
        total_loss = abs(sum(t.pnl_amount for t in losers if t.pnl_amount)) or 0
        profit_factor = (total_profit / total_loss) if total_loss > 0 else (999.0 if total_profit > 0 else 0)
        max_dd = max((t.max_drawdown for t in closed if t.max_drawdown), default=0)

        signals_approved = len([d for d in decisions if d.decision == "APPROVED"])

        postmortems = []
        for t in closed:
            postmortems.append({
                "symbol": t.symbol, "direction": t.direction,
                "entry_price": t.entry_price, "exit_price": t.exit_price,
                "pnl": t.pnl_amount, "exit_reason": t.exit_reason,
            })

        narrative = (
            f"Day summary: {total} trades executed, {len(winners)} wins, {len(losers)} losses. "
            f"Win rate: {win_rate:.1f}%. Total PnL: {format_pnl(total_pnl)}. "
            f"Profit factor: {profit_factor:.2f}."
        )

        now = now_ist().replace(tzinfo=None)

        existing = db.query(DailySummary).filter(DailySummary.trade_date == trade_date).first()
        summary = existing if existing else DailySummary(trade_date=trade_date)
        if not existing:
            db.add(summary)

        summary.total_trades = total
        summary.winning_trades = len(winners)
        summary.losing_trades = len(losers)
        summary.win_rate = win_rate
        summary.total_pnl = total_pnl
        summary.profit_factor = profit_factor
        summary.max_drawdown = max_dd
        summary.signals_shown = len(signals)
        summary.signals_approved = signals_approved
        summary.signals_skipped = len(signals) - signals_approved
        summary.daily_narrative = narrative
        summary.trade_postmortems = json.dumps(postmortems)
        summary.created_at = now

        db.commit()
        db.refresh(summary)

        print(f"Saved summary: {total} trades, PnL: {format_pnl(total_pnl)}, Win Rate: {win_rate:.1f}%")
        return summary.to_dict()
    except Exception as e:
        db.rollback()
        print(f"Error generating summary: {e}")
        return None
    finally:
        db.close()


def mine_patterns(trade_date: date) -> list[dict]:
    """Mine patterns from recent trade history."""
    print(f"=== Mining patterns for {trade_date} ===")

    db = SessionLocal()
    try:
        lookback_start = trade_date - timedelta(days=LOOKBACK_DAYS)
        trades = (
            db.query(PaperTrade)
            .filter(PaperTrade.trade_date.between(lookback_start, trade_date), PaperTrade.status == "CLOSED")
            .all()
        )

        if len(trades) < PATTERN_MIN_OCCURRENCES:
            print(f"Not enough trades ({len(trades)}) for pattern mining (min: {PATTERN_MIN_OCCURRENCES}).")
            return []

        learnings = []

        # Pattern 1: Direction bias
        by_direction: dict[str, list] = {}
        for t in trades:
            if t.direction:
                by_direction.setdefault(t.direction, []).append(t)

        for direction, dir_trades in by_direction.items():
            if len(dir_trades) < PATTERN_MIN_OCCURRENCES:
                continue
            wins = sum(1 for t in dir_trades if t.pnl_amount and t.pnl_amount > 0)
            win_rate = wins / len(dir_trades)
            confidence = _calc_confidence(len(dir_trades), win_rate)

            if confidence >= MIN_CONFIDENCE:
                learning = StrategyLearning(
                    learning_date=trade_date,
                    category="DIRECTION_BIAS",
                    insight=f"{direction} trades have {win_rate*100:.0f}% win rate over last {LOOKBACK_DAYS} days ({len(dir_trades)} trades)",
                    evidence=json.dumps({"direction": direction, "trades": len(dir_trades), "win_rate": win_rate}),
                    confidence=confidence,
                    times_validated=0,
                    is_active=True,
                    created_at=now_ist().replace(tzinfo=None),
                )
                db.add(learning)
                learnings.append(learning)

        # Pattern 2: Early entry timing
        early = [t for t in trades if t.entry_time and t.entry_time.time() < dt_time(9, 30)]
        if len(early) >= PATTERN_MIN_OCCURRENCES:
            early_wins = sum(1 for t in early if t.pnl_amount and t.pnl_amount > 0)
            early_wr = early_wins / len(early)
            confidence = _calc_confidence(len(early), early_wr)
            if confidence >= MIN_CONFIDENCE:
                learning = StrategyLearning(
                    learning_date=trade_date,
                    category="TIMING",
                    insight=f"Early entries (before 9:30) have {early_wr*100:.0f}% win rate ({len(early)} trades)",
                    evidence=json.dumps({"timing": "early", "trades": len(early), "win_rate": early_wr}),
                    confidence=confidence,
                    times_validated=0,
                    is_active=True,
                    created_at=now_ist().replace(tzinfo=None),
                )
                db.add(learning)
                learnings.append(learning)

        db.commit()
        print(f"=== Mined {len(learnings)} patterns ===")
        return [l.to_dict() for l in learnings]
    except Exception as e:
        db.rollback()
        print(f"Error mining patterns: {e}")
        return []
    finally:
        db.close()


def _calc_confidence(sample_size: int, win_rate: float) -> float:
    size_factor = min(1.0, sample_size / 20.0)
    rate_factor = abs(win_rate - 0.5) * 2
    return min(1.0, size_factor * 0.6 + rate_factor * 0.4)


# --- REST Endpoints ---
@app.route("/api/learner/health")
def health():
    return jsonify({"status": "UP", "agent": "learning-summary", "time": str(now_ist())})


@app.route("/api/learner/generate-summary", methods=["POST"])
def api_generate_summary():
    trade_date_str = request.args.get("date", str(today_ist()))
    result = generate_daily_summary(date.fromisoformat(trade_date_str))
    if result is None:
        return "", 204
    return jsonify(result)


@app.route("/api/learner/mine-patterns", methods=["POST"])
def api_mine_patterns():
    trade_date_str = request.args.get("date", str(today_ist()))
    result = mine_patterns(date.fromisoformat(trade_date_str))
    return jsonify(result)


@app.route("/api/learner/summary/<trade_date>")
def get_summary(trade_date):
    d = date.fromisoformat(trade_date)
    db = SessionLocal()
    try:
        s = db.query(DailySummary).filter(DailySummary.trade_date == d).first()
        if not s:
            return "", 204
        return jsonify(s.to_dict())
    finally:
        db.close()


@app.route("/api/learner/learnings")
def get_learnings():
    db = SessionLocal()
    try:
        learnings = (
            db.query(StrategyLearning)
            .filter(StrategyLearning.is_active == True)  # noqa: E712
            .order_by(StrategyLearning.confidence.desc())
            .all()
        )
        return jsonify([l.to_dict() for l in learnings])
    finally:
        db.close()


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8083))
    print(f"Starting Learning Summary on port {port}")
    app.run(host="0.0.0.0", port=port, debug=True)
