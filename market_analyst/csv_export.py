"""Exports StockAnalysis records to CSV files."""

import csv
import os
from datetime import date
from pathlib import Path

CSV_HEADERS = [
    "trade_date", "symbol", "gap_percent", "gap_direction", "gap_category", "gap_score", "atr_ratio",
    "sentiment_score", "sentiment_level", "sentiment_reasoning",
    "volume_ratio", "volume_level", "volume_score", "vwap_position",
    "pcr", "oi_buildup", "max_pain", "iv_percentile", "oi_score", "suggested_strike",
    "composite_score", "signal_direction", "recommended_action", "confidence_level",
    "entry_strike", "estimated_premium", "stop_loss", "target", "risk_reward_ratio",
    "claude_reasoning", "risk_warnings",
]


def export_to_csv(analyses: list, output_dir: str = None) -> Path:
    """Export list of StockAnalysis (ORM objects or dicts) to a dated CSV file."""
    if output_dir is None:
        output_dir = os.environ.get("CSV_OUTPUT_DIR", "./output")

    Path(output_dir).mkdir(parents=True, exist_ok=True)

    trade_date = None
    if analyses:
        first = analyses[0]
        trade_date = first.trade_date if hasattr(first, "trade_date") else first.get("trade_date")
    if trade_date is None:
        trade_date = date.today()

    filename = f"trade-signals-{trade_date}.csv"
    filepath = Path(output_dir) / filename

    with open(filepath, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(CSV_HEADERS)
        for sa in analyses:
            writer.writerow(_to_row(sa))

    print(f"Exported {len(analyses)} signals to {filepath.absolute()}")
    return filepath


def _to_row(sa) -> list:
    """Convert StockAnalysis ORM object or dict to CSV row."""
    if hasattr(sa, "__table__"):
        d = {c.name: getattr(sa, c.name) for c in sa.__table__.columns}
    else:
        d = sa
    return [d.get(h, "") or "" for h in CSV_HEADERS]
