from datetime import datetime, date, time, timedelta
from zoneinfo import ZoneInfo

IST = ZoneInfo("Asia/Kolkata")

PRE_MARKET_OPEN = time(9, 0)
MARKET_OPEN = time(9, 15)
MARKET_CLOSE = time(15, 30)


def now_ist() -> datetime:
    return datetime.now(IST)


def today_ist() -> date:
    return now_ist().date()


def is_pre_market_window() -> bool:
    now = now_ist()
    t = now.time()
    return is_weekday(now.date()) and PRE_MARKET_OPEN <= t < MARKET_OPEN


def is_market_open() -> bool:
    now = now_ist()
    t = now.time()
    return is_weekday(now.date()) and MARKET_OPEN <= t <= MARKET_CLOSE


def is_market_closed() -> bool:
    return not is_pre_market_window() and not is_market_open()


def is_weekday(d: date = None) -> bool:
    if d is None:
        d = today_ist()
    return d.weekday() < 5  # Mon=0 .. Fri=4


def get_current_expiry() -> date:
    today = today_ist()
    days_until_thursday = (3 - today.weekday()) % 7
    thursday = today + timedelta(days=days_until_thursday)
    if today == thursday and now_ist().time() > MARKET_CLOSE:
        thursday += timedelta(days=7)
    if today.weekday() > 3:
        thursday = today + timedelta(days=(3 - today.weekday()) % 7 + 7)
    return thursday
