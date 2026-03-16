"""Trade Dashboard — Flask app on port 8080.

Upload CSV signals, view dashboard, approve trades for execution.
"""

import os
from datetime import date

from flask import Flask, request, render_template, redirect, url_for, flash, jsonify

from shared.database import SessionLocal
from shared.models import StockAnalysis, TradeDecision
from shared.time_utils import today_ist, now_ist
from shared.lot_sizes import get_lot_size
from trade_dashboard.csv_parser import parse_csv

app = Flask(__name__)
app.secret_key = os.environ.get("SECRET_KEY", "pretrade-dashboard-secret")


@app.route("/")
def index():
    return redirect(url_for("dashboard", date=str(today_ist())))


@app.route("/dashboard")
def dashboard():
    trade_date_str = request.args.get("date", str(today_ist()))
    trade_date = date.fromisoformat(trade_date_str)

    db = SessionLocal()
    try:
        signals = (
            db.query(StockAnalysis)
            .filter(StockAnalysis.trade_date == trade_date)
            .order_by(StockAnalysis.composite_score.desc().nullslast())
            .all()
        )
        decisions = (
            db.query(TradeDecision)
            .filter(TradeDecision.trade_date == trade_date)
            .all()
        )
        approved_ids = {d.signal_id for d in decisions if d.decision == "APPROVED"}

        return render_template(
            "dashboard.html",
            signals=signals,
            approved_ids=approved_ids,
            trade_date=trade_date_str,
            current_time=str(now_ist()),
        )
    finally:
        db.close()


@app.route("/upload", methods=["GET"])
def upload_page():
    return render_template("upload.html")


@app.route("/upload", methods=["POST"])
def handle_upload():
    file = request.files.get("file")
    if not file or file.filename == "":
        flash("Please select a CSV file to upload.", "error")
        return redirect(url_for("upload_page"))

    try:
        content = file.read()
        signals = parse_csv(content)
    except Exception as e:
        flash(f"Failed to parse CSV: {e}", "error")
        return redirect(url_for("upload_page"))

    if not signals:
        flash("No valid signals found in the CSV file.", "error")
        return redirect(url_for("upload_page"))

    db = SessionLocal()
    saved, updated = 0, 0
    try:
        for sig in signals:
            existing = (
                db.query(StockAnalysis)
                .filter(StockAnalysis.trade_date == sig["trade_date"], StockAnalysis.symbol == sig["symbol"])
                .first()
            )
            if existing:
                for k, v in sig.items():
                    if k != "id":
                        setattr(existing, k, v)
                updated += 1
            else:
                sa = StockAnalysis(**sig)
                db.add(sa)
                saved += 1

        db.commit()
        trade_date_str = str(signals[0]["trade_date"])
        flash(f"Imported {saved} new signals, updated {updated} existing.", "success")
        return redirect(url_for("dashboard", date=trade_date_str))
    except Exception as e:
        db.rollback()
        flash(f"Database error: {e}", "error")
        return redirect(url_for("upload_page"))
    finally:
        db.close()


@app.route("/approve-trades", methods=["POST"])
def approve_trades():
    signal_ids = request.form.getlist("signal_ids", type=int)
    trade_date_str = request.form.get("trade_date", str(today_ist()))
    trade_date = date.fromisoformat(trade_date_str)

    if not signal_ids:
        flash("No trades selected.", "error")
        return redirect(url_for("dashboard", date=trade_date_str))

    db = SessionLocal()
    approved = 0
    try:
        for signal_id in signal_ids:
            signal = db.query(StockAnalysis).get(signal_id)
            if not signal:
                continue

            existing = (
                db.query(TradeDecision)
                .filter(
                    TradeDecision.trade_date == trade_date,
                    TradeDecision.signal_id == signal_id,
                    TradeDecision.decision == "APPROVED",
                )
                .first()
            )
            if existing:
                continue

            decision = TradeDecision(
                trade_date=trade_date,
                signal_id=signal_id,
                symbol=signal.symbol,
                decision="APPROVED",
                decision_time=now_ist().replace(tzinfo=None),
                decision_reason="Approved via dashboard",
                final_strike=signal.entry_strike,
                final_sl=signal.stop_loss,
                final_target=signal.target,
                final_lot_size=get_lot_size(signal.symbol),
                final_direction=signal.signal_direction,
            )
            db.add(decision)
            approved += 1

        db.commit()
        flash(f"Approved {approved} trade(s) for execution.", "success")
    except Exception as e:
        db.rollback()
        flash(f"Error approving trades: {e}", "error")
    finally:
        db.close()

    return redirect(url_for("dashboard", date=trade_date_str))


@app.route("/api/dashboard/health")
def health():
    return jsonify({"status": "UP", "agent": "trade-dashboard", "time": str(now_ist())})


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8080))
    print(f"Starting Trade Dashboard on port {port}")
    app.run(host="0.0.0.0", port=port, debug=True)
