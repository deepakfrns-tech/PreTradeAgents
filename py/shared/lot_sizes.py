"""NSE F&O lot size registry."""

LOT_SIZES = {
    "NIFTY": 50, "BANKNIFTY": 15, "FINNIFTY": 40, "MIDCPNIFTY": 75,
    "RELIANCE": 250, "TCS": 175, "INFY": 300, "HDFCBANK": 550,
    "ICICIBANK": 1375, "HINDUNILVR": 300, "ITC": 1600, "SBIN": 1500,
    "BHARTIARTL": 950, "KOTAKBANK": 400, "LT": 375, "AXISBANK": 1200,
    "ASIANPAINT": 300, "MARUTI": 100, "TITAN": 375, "SUNPHARMA": 700,
    "BAJFINANCE": 125, "BAJAJFINSV": 500, "WIPRO": 1500, "HCLTECH": 350,
    "ULTRACEMCO": 100, "NESTLEIND": 50, "TATAMOTORS": 1425, "POWERGRID": 2700,
    "NTPC": 2700, "M_M": 700, "ONGC": 3850, "TATASTEEL": 5500,
    "TECHM": 600, "INDUSINDBK": 900, "JSWSTEEL": 1350, "DRREDDY": 125,
    "DIVISLAB": 200, "ADANIENT": 500, "ADANIPORTS": 1250, "GRASIM": 475,
    "CIPLA": 650, "EICHERMOT": 350, "HEROMOTOCO": 300, "APOLLOHOSP": 250,
    "BPCL": 1800, "COALINDIA": 2100, "SBILIFE": 750, "TATACONSUM": 900,
    "BRITANNIA": 200, "HDFCLIFE": 1100, "UPL": 1300, "BAJAJ_AUTO": 250,
}


def get_lot_size(symbol: str) -> int:
    return LOT_SIZES.get(symbol.upper(), -1)


def is_valid_fno_stock(symbol: str) -> bool:
    return symbol.upper() in LOT_SIZES


def get_all_symbols() -> set:
    return set(LOT_SIZES.keys())
