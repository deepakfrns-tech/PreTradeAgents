"""Market Analyst Agent — Flask app on port 8081.

Provides REST endpoints for stock analysis signals and CSV export.
Collects NSE pre-market data, scrapes news sentiment, analyzes options flow,
and produces Claude-scored trading signals saved to the stock_analysis table.
"""

import logging
import os
from datetime import date

from flask import Flask, request, jsonify, send_file

from shared.database import SessionLocal
from shared.models import StockAnalysis
from shared.time_utils import today_ist, now_ist
from market_analyst.csv_export import export_to_csv
from market_analyst.pipeline import run_pipeline, run_pipeline_with_sample_data

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(name)s] %(levelname)s: %(message)s")

app = Flask(__name__)


@app.route("/")
def index():
    return jsonify({
        "agent": "market-analyst",
        "status": "UP",
        "endpoints": {
            "health": "/api/analyst/health",
            "run_pipeline": "POST /api/analyst/run?date=YYYY-MM-DD&min_gap=0.5&top_n=10  (needs NSE market hours)",
            "run_test": "POST /api/analyst/run-test?date=YYYY-MM-DD  (works anytime with sample data)",
            "signals": "/api/analyst/signals/<trade_date>  (e.g. /api/analyst/signals/2026-03-16)",
            "export_csv": "POST /api/analyst/export-csv?date=YYYY-MM-DD",
        },
    })


@app.route("/api/analyst/health")
def health():
    return jsonify({"status": "UP", "agent": "market-analyst", "time": str(now_ist())})


@app.route("/api/analyst/run", methods=["POST"])
def run_analysis():
    trade_date_str = request.args.get("date", str(today_ist()))
    d = date.fromisoformat(trade_date_str)
    min_gap = request.args.get("min_gap", type=float)
    top_n = request.args.get("top_n", type=int)

    result = run_pipeline(trade_date=d, min_gap=min_gap, top_n=top_n)

    status_code = 200 if result["status"] == "ok" else 500
    return jsonify(result), status_code


@app.route("/api/analyst/run-test", methods=["POST"])
def run_test():
    trade_date_str = request.args.get("date", str(today_ist()))
    d = date.fromisoformat(trade_date_str)

    result = run_pipeline_with_sample_data(trade_date=d)

    status_code = 200 if result["status"] == "ok" else 500
    return jsonify(result), status_code


@app.route("/api/analyst/signals/<trade_date>")
def get_signals(trade_date):
    db = SessionLocal()
    try:
        d = date.fromisoformat(trade_date)
        signals = (
            db.query(StockAnalysis)
            .filter(StockAnalysis.trade_date == d)
            .order_by(StockAnalysis.composite_score.desc().nullslast())
            .all()
        )
        return jsonify([s.to_dict() for s in signals])
    finally:
        db.close()


@app.route("/api/analyst/export-csv", methods=["POST"])
def export_csv():
    trade_date = request.args.get("date", str(today_ist()))
    d = date.fromisoformat(trade_date)

    db = SessionLocal()
    try:
        signals = (
            db.query(StockAnalysis)
            .filter(StockAnalysis.trade_date == d)
            .order_by(StockAnalysis.composite_score.desc().nullslast())
            .all()
        )

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
