"""Market Analyst Agent — Flask app on port 8081.

Provides REST endpoints for stock analysis signals and CSV export.
"""
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from datetime import date
from flask import Flask, request, jsonify, send_file
from shared.database import SessionLocal
from shared.models import StockAnalysis
from shared.time_utils import today_ist, now_ist
from market_analyst.csv_export import export_to_csv

app = Flask(__name__)


@app.route("/api/analyst/health")
def health():
    return jsonify({"status": "UP", "agent": "market-analyst", "time": str(now_ist())})


@app.route("/api/analyst/signals/<trade_date>")
def get_signals(trade_date):
    db = SessionLocal()
    try:
        d = date.fromisoformat(trade_date)
        signals = db.query(StockAnalysis).filter(
            StockAnalysis.trade_date == d
        ).order_by(StockAnalysis.composite_score.desc().nullslast()).all()
        return jsonify([s.to_dict() for s in signals])
    finally:
        db.close()


@app.route("/api/analyst/export-csv", methods=["POST"])
def export_csv():
    trade_date = request.args.get("date", str(today_ist()))
    d = date.fromisoformat(trade_date)

    db = SessionLocal()
    try:
        signals = db.query(StockAnalysis).filter(
            StockAnalysis.trade_date == d
        ).order_by(StockAnalysis.composite_score.desc().nullslast()).all()

        if not signals:
            return jsonify({"error": "No signals for this date"}), 204

        filepath = export_to_csv(signals)
        return send_file(str(filepath.absolute()), as_attachment=True, download_name=filepath.name)
    finally:
        db.close()


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8081))
    print(f"Starting Market Analyst on port {port}")
    app.run(host="0.0.0.0", port=port, debug=True)
