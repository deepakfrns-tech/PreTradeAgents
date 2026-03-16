"""Parses trade-signals CSV files into StockAnalysis dictionaries."""

import csv
import io
from datetime import date

CSV_HEADERS = [
    "trade_date", "symbol", "gap_percent", "gap_direction", "gap_category", "gap_score", "atr_ratio",
    "sentiment_score", "sentiment_level", "sentiment_reasoning",
    "volume_ratio", "volume_level", "volume_score", "vwap_position",
    "pcr", "oi_buildup", "max_pain", "iv_percentile", "oi_score", "suggested_strike",
    "composite_score", "signal_direction", "recommended_action", "confidence_level",
    "entry_strike", "estimated_premium", "stop_loss", "target", "risk_reward_ratio",
    "claude_reasoning", "risk_warnings",
]

FLOAT_FIELDS = {
    "gap_percent", "gap_score", "atr_ratio", "sentiment_score",
    "volume_ratio", "volume_score", "vwap_position",
    "pcr", "max_pain", "iv_percentile", "oi_score", "suggested_strike",
    "composite_score", "entry_strike", "estimated_premium", "stop_loss",
    "target", "risk_reward_ratio",
}


def parse_csv(file_content: bytes) -> list[dict]:
    """Parse CSV bytes into list of signal dicts."""
    text = file_content.decode("utf-8")
    reader = csv.DictReader(io.StringIO(text))

    signals = []
    for row in reader:
        signal = {}
        for key in CSV_HEADERS:
            val = row.get(key, "").strip()
            if key == "trade_date":
                signal[key] = date.fromisoformat(val) if val else None
            elif key in FLOAT_FIELDS:
                signal[key] = float(val) if val else None
            else:
                signal[key] = val if val else None
        signals.append(signal)

    return signals
