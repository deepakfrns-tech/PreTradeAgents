"""Tests for trade_dashboard.csv_parser module."""

from datetime import date
from trade_dashboard.csv_parser import parse_csv


def test_parse_csv_basic():
    csv_content = (
        "trade_date,symbol,gap_percent,gap_direction,gap_category,gap_score,atr_ratio,"
        "sentiment_score,sentiment_level,sentiment_reasoning,"
        "volume_ratio,volume_level,volume_score,vwap_position,"
        "pcr,oi_buildup,max_pain,iv_percentile,oi_score,suggested_strike,"
        "composite_score,signal_direction,recommended_action,confidence_level,"
        "entry_strike,estimated_premium,stop_loss,target,risk_reward_ratio,"
        "claude_reasoning,risk_warnings\n"
        "2026-03-15,NIFTY,1.5,UP,MODERATE,7.5,1.2,"
        "8.0,HIGH,Bullish sentiment,"
        "1.5,ABOVE_AVG,7.0,0.5,"
        "1.1,LONG_BUILD,22500,45.0,8.0,22600,"
        "78.5,BULLISH,BUY CE,HIGH,"
        "22600,150,22400,22800,2.0,"
        "Strong gap with good OI support,VIX elevated\n"
    ).encode("utf-8")

    signals = parse_csv(csv_content)
    assert len(signals) == 1
    assert signals[0]["symbol"] == "NIFTY"
    assert signals[0]["trade_date"] == date(2026, 3, 15)
    assert signals[0]["gap_percent"] == 1.5
    assert signals[0]["composite_score"] == 78.5
    assert signals[0]["signal_direction"] == "BULLISH"


def test_parse_csv_empty():
    csv_content = b"trade_date,symbol\n"
    signals = parse_csv(csv_content)
    assert signals == []
