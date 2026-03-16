"""Tests for shared.time_utils module."""

from datetime import date, datetime, time, timedelta
from zoneinfo import ZoneInfo

from shared.time_utils import (
    IST, now_ist, today_ist, is_weekday,
    get_current_expiry, get_next_expiry, format_timestamp,
)


def test_now_ist_returns_ist_timezone():
    result = now_ist()
    assert result.tzinfo is not None
    assert str(result.tzinfo) == "Asia/Kolkata"


def test_today_ist_returns_date():
    result = today_ist()
    assert isinstance(result, date)


def test_is_weekday_monday():
    # 2026-03-16 is a Monday
    assert is_weekday(date(2026, 3, 16)) is True


def test_is_weekday_saturday():
    # 2026-03-14 is a Saturday
    assert is_weekday(date(2026, 3, 14)) is False


def test_is_weekday_sunday():
    # 2026-03-15 is a Sunday
    assert is_weekday(date(2026, 3, 15)) is False


def test_get_current_expiry_returns_thursday():
    expiry = get_current_expiry()
    assert expiry.weekday() == 3  # Thursday


def test_get_next_expiry_is_one_week_after_current():
    current = get_current_expiry()
    next_exp = get_next_expiry()
    assert next_exp == current + timedelta(days=7)


def test_format_timestamp_with_datetime():
    dt = datetime(2026, 3, 14, 9, 15, 0)
    result = format_timestamp(dt)
    assert result == "14-Mar-2026 09:15:00"


def test_format_timestamp_none():
    assert format_timestamp(None) == "N/A"
