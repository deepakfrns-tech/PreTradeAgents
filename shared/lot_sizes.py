"""NSE F&O (Futures and Options) lot size registry.

Provides lookup methods for lot sizes and validation of F&O-eligible symbols.
"""

import logging

logger = logging.getLogger(__name__)

LOT_SIZES: dict[str, int] = {
    # Indices
    "NIFTY": 50,
    "BANKNIFTY": 15,
    "FINNIFTY": 40,
    "MIDCPNIFTY": 75,
    # Large-cap stocks
    "RELIANCE": 250,
    "TCS": 175,
    "INFY": 300,
    "HDFCBANK": 550,
    "ICICIBANK": 1375,
    "BAJFINANCE": 125,
    "SBIN": 1500,
    "ITC": 1600,
    "TATAMOTORS": 1400,
    "HINDUNILVR": 300,
    "KOTAKBANK": 400,
    "LT": 375,
    "AXISBANK": 625,
    "BHARTIARTL": 475,
    "ASIANPAINT": 300,
    "MARUTI": 100,
    "HCLTECH": 350,
    "SUNPHARMA": 700,
    "TITAN": 375,
    "ULTRACEMCO": 100,
    "WIPRO": 1500,
    "ADANIENT": 500,
    "ADANIPORTS": 1250,
    "BAJAJ-AUTO": 250,
    "BAJAJFINSV": 500,
    "BPCL": 1800,
    "BRITANNIA": 200,
    "CIPLA": 650,
    "COALINDIA": 2100,
    "DIVISLAB": 200,
    "DRREDDY": 125,
    "EICHERMOT": 175,
    "GRASIM": 350,
    "HDFCLIFE": 1100,
    "HEROMOTOCO": 300,
    "HINDALCO": 1400,
    "INDUSINDBK": 500,
    "JSWSTEEL": 1350,
    "M&M": 700,
    "NESTLEIND": 50,
    "NTPC": 2800,
    "ONGC": 3850,
    "POWERGRID": 2700,
    "SBILIFE": 750,
    "TATACONSUM": 675,
    "TATASTEEL": 5500,
    "TECHM": 600,
    "APOLLOHOSP": 250,
    "DMART": 200,
    "LTIM": 150,
    "PIDILITIND": 500,
    "SIEMENS": 275,
    "HAL": 300,
    "IOC": 4300,
    "PNB": 6000,
    "BANKBARODA": 2925,
    "ZOMATO": 6250,
    "TATAPOWER": 2700,
    "DLF": 1375,
    "BEL": 3700,
}


def get_lot_size(symbol: str) -> int:
    """Returns the F&O lot size for the given symbol. Returns -1 if not found."""
    if symbol is None:
        return -1
    lot_size = LOT_SIZES.get(symbol.upper().strip())
    if lot_size is None:
        logger.warning("Lot size not found for symbol: %s", symbol)
        return -1
    return lot_size


def is_valid_fno_stock(symbol: str) -> bool:
    """Checks whether a symbol is a valid F&O stock/index."""
    if symbol is None:
        return False
    return symbol.upper().strip() in LOT_SIZES


def get_all_symbols() -> set[str]:
    """Returns all registered F&O symbols."""
    return set(LOT_SIZES.keys())


def get_all_lot_sizes() -> dict[str, int]:
    """Returns the entire lot size map (read-only copy)."""
    return dict(LOT_SIZES)
