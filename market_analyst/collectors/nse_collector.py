"""Collects NSE pre-market data, option chains, and market snapshots.

WARNING: NSE aggressively rate-limits. Always warm up session before API calls.
"""

import logging
from dataclasses import dataclass, field

import requests

logger = logging.getLogger(__name__)

NSE_BASE_URL = "https://www.nseindia.com"
NSE_PRE_MARKET_URL = NSE_BASE_URL + "/api/market-data-pre-open?key=NIFTY"
NSE_OPTION_CHAIN_URL = NSE_BASE_URL + "/api/option-chain-indices?symbol="
NSE_EQUITY_OPTION_CHAIN_URL = NSE_BASE_URL + "/api/option-chain-equities?symbol="
NSE_INDEX_URL = NSE_BASE_URL + "/api/allIndices"

USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
DEFAULT_TIMEOUT = 30


@dataclass
class PreMarketEntry:
    symbol: str = ""
    previous_close: float = 0.0
    iep: float = 0.0  # Indicative Equilibrium Price
    change: float = 0.0
    change_percent: float = 0.0
    final_quantity: int = 0
    total_buy_quantity: int = 0
    total_sell_quantity: int = 0
    last_price: float = 0.0
    year_high: float = 0.0
    year_low: float = 0.0

    @property
    def gap_percent(self) -> float:
        if self.previous_close == 0:
            return 0.0
        return ((self.iep - self.previous_close) / self.previous_close) * 100.0


@dataclass
class OptionEntry:
    strike_price: float = 0.0
    expiry_date: str = ""
    call_oi: int = 0
    call_change_in_oi: int = 0
    call_ltp: float = 0.0
    call_iv: float = 0.0
    call_volume: int = 0
    put_oi: int = 0
    put_change_in_oi: int = 0
    put_ltp: float = 0.0
    put_iv: float = 0.0
    put_volume: int = 0


@dataclass
class OptionChainData:
    symbol: str = ""
    underlying_value: float = 0.0
    expiry_dates: list[str] = field(default_factory=list)
    entries: list[OptionEntry] = field(default_factory=list)


@dataclass
class MarketSnapshotData:
    nifty_value: float = 0.0
    nifty_change: float = 0.0
    bank_nifty_value: float = 0.0
    bank_nifty_change: float = 0.0
    india_vix: float = 0.0
    advances: int = 0
    declines: int = 0
    advance_decline_ratio: float = 0.0


def _build_session(timeout: int = DEFAULT_TIMEOUT) -> requests.Session:
    session = requests.Session()
    session.headers.update({
        "User-Agent": USER_AGENT,
        "Accept": "application/json, text/plain, */*",
        "Accept-Language": "en-US,en;q=0.9",
        "Accept-Encoding": "gzip, deflate, br",
    })
    # Warm up session
    try:
        session.get(NSE_BASE_URL, timeout=timeout)
    except Exception:
        pass
    return session


def collect_pre_market_data(timeout: int = DEFAULT_TIMEOUT) -> list[PreMarketEntry]:
    """Fetches pre-market data for NIFTY 50 constituents."""
    logger.info("Collecting NSE pre-market data...")
    try:
        session = _build_session(timeout)
        response = session.get(NSE_PRE_MARKET_URL, timeout=timeout)
        response.raise_for_status()
        return _parse_pre_market(response.json())
    except Exception as e:
        logger.error("Failed to collect NSE pre-market data: %s", e)
        return []


def collect_option_chain(symbol: str, timeout: int = DEFAULT_TIMEOUT) -> OptionChainData:
    """Fetches the full option chain for a given symbol."""
    logger.info("Collecting option chain for symbol: %s", symbol)
    try:
        session = _build_session(timeout)
        if symbol.upper() in {"NIFTY", "BANKNIFTY", "FINNIFTY", "MIDCPNIFTY"}:
            url = NSE_OPTION_CHAIN_URL + symbol
        else:
            url = NSE_EQUITY_OPTION_CHAIN_URL + symbol
        response = session.get(url, timeout=timeout)
        response.raise_for_status()
        return _parse_option_chain(response.json(), symbol)
    except Exception as e:
        logger.error("Failed to collect option chain for %s: %s", symbol, e)
        return OptionChainData(symbol=symbol)


def collect_market_snapshot(timeout: int = DEFAULT_TIMEOUT) -> MarketSnapshotData:
    """Fetches broad market snapshot: index values, VIX, advance/decline."""
    logger.info("Collecting market snapshot...")
    try:
        session = _build_session(timeout)
        response = session.get(NSE_INDEX_URL, timeout=timeout)
        response.raise_for_status()
        return _parse_market_snapshot(response.json())
    except Exception as e:
        logger.error("Failed to collect market snapshot: %s", e)
        return MarketSnapshotData()


def _parse_pre_market(data: dict) -> list[PreMarketEntry]:
    entries = []
    for item in data.get("data", []):
        metadata = item.get("metadata", {})
        detail = item.get("detail", {}).get("preOpenMarket", {})
        entries.append(PreMarketEntry(
            symbol=metadata.get("symbol", ""),
            previous_close=metadata.get("previousClose", 0),
            iep=metadata.get("iep", 0),
            change=metadata.get("change", 0),
            change_percent=metadata.get("pChange", 0),
            final_quantity=metadata.get("finalQuantity", 0),
            total_buy_quantity=detail.get("totalBuyQuantity", 0),
            total_sell_quantity=detail.get("totalSellQuantity", 0),
            last_price=metadata.get("lastPrice", 0),
            year_high=metadata.get("yearHigh", 0),
            year_low=metadata.get("yearLow", 0),
        ))
    return entries


def _parse_option_chain(data: dict, symbol: str) -> OptionChainData:
    result = OptionChainData(symbol=symbol)
    records = data.get("records", {})
    result.underlying_value = records.get("underlyingValue", 0)
    result.expiry_dates = records.get("expiryDates", [])

    for item in records.get("data", []):
        entry = OptionEntry(
            strike_price=item.get("strikePrice", 0),
            expiry_date=item.get("expiryDate", ""),
        )
        if "CE" in item:
            ce = item["CE"]
            entry.call_oi = ce.get("openInterest", 0)
            entry.call_change_in_oi = ce.get("changeinOpenInterest", 0)
            entry.call_ltp = ce.get("lastPrice", 0)
            entry.call_iv = ce.get("impliedVolatility", 0)
            entry.call_volume = ce.get("totalTradedVolume", 0)
        if "PE" in item:
            pe = item["PE"]
            entry.put_oi = pe.get("openInterest", 0)
            entry.put_change_in_oi = pe.get("changeinOpenInterest", 0)
            entry.put_ltp = pe.get("lastPrice", 0)
            entry.put_iv = pe.get("impliedVolatility", 0)
            entry.put_volume = pe.get("totalTradedVolume", 0)
        result.entries.append(entry)

    return result


def _parse_market_snapshot(data: dict) -> MarketSnapshotData:
    snapshot = MarketSnapshotData()
    for item in data.get("data", []):
        index_name = item.get("index", "")
        if index_name == "NIFTY 50":
            snapshot.nifty_value = item.get("last", 0)
            snapshot.nifty_change = item.get("percentChange", 0)
            snapshot.advances = item.get("advances", 0)
            snapshot.declines = item.get("declines", 0)
        elif index_name == "NIFTY BANK":
            snapshot.bank_nifty_value = item.get("last", 0)
            snapshot.bank_nifty_change = item.get("percentChange", 0)
        elif index_name == "INDIA VIX":
            snapshot.india_vix = item.get("last", 0)

    if snapshot.advances > 0 or snapshot.declines > 0:
        snapshot.advance_decline_ratio = snapshot.advances / max(1, snapshot.declines)
    return snapshot
