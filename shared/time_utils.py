"""IST (Indian Standard Time) utilities for NSE market hours and expiry calculations.

All time operations in the system MUST go through this module.
Never use datetime.now() directly — always use now_ist().
"""

from datetime import datetime, date, time, timedelta
from zoneinfo import ZoneInfo

IST = ZoneInfo("Asia/Kolkata")

PRE_MARKET_OPEN = time(9, 0)
MARKET_OPEN = time(9, 15)
MARKET_CLOSE = time(15, 30)


def now_ist() -> datetime:
    """Returns the current datetime in IST."""
    return datetime.now(IST)


def today_ist() -> date:
    """Returns today's date in IST."""
    return now_ist().date()


def is_pre_market_window() -> bool:
    """Checks if current time is in the pre-market window (9:00-9:15 AM IST on weekdays)."""
    now = now_ist()
    t = now.time()
    return is_weekday(now.date()) and PRE_MARKET_OPEN <= t < MARKET_OPEN


def is_market_open() -> bool:
    """Checks if the market is currently open (9:15 AM - 3:30 PM IST on weekdays)."""
    now = now_ist()
    t = now.time()
    return is_weekday(now.date()) and MARKET_OPEN <= t <= MARKET_CLOSE


def is_market_closed() -> bool:
    """Checks if the market is currently closed."""
    return not is_pre_market_window() and not is_market_open()


def is_weekday(d: date = None) -> bool:
    """Checks if a date is a weekday (Monday-Friday)."""
    if d is None:
        d = today_ist()
    return d.weekday() < 5


def get_current_expiry() -> date:
    """Calculates the current weekly expiry date (Thursday).

    If today is past Thursday, returns next Thursday.
    If today is Thursday and market is closed, returns next Thursday.
    """
    now = now_ist()
    today = now.date()
    days_until_thursday = (3 - today.weekday()) % 7
    thursday = today + timedelta(days=days_until_thursday)

    if today == thursday and now.time() > MARKET_CLOSE:
        thursday += timedelta(days=7)
    if today.weekday() > 3:
        thursday = today + timedelta(days=(3 - today.weekday()) % 7 + 7)

    return thursday


def get_next_expiry() -> date:
    """Returns the next weekly expiry (Thursday after current expiry)."""
    return get_current_expiry() + timedelta(days=7)


def format_timestamp(dt: datetime) -> str:
    """Formats a datetime as 'dd-MMM-yyyy HH:mm:ss'."""
    if dt is None:
        return "N/A"
    return dt.strftime("%d-%b-%Y %H:%M:%S")
