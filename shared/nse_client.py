"""HTTP client for NSE (National Stock Exchange) API calls.

Handles NSE-specific headers, session warmup, and retry logic with exponential backoff.
Always warm up session (hit base URL) before API calls — NSE requires it.
"""

import logging
import time

import requests

logger = logging.getLogger(__name__)

NSE_BASE_URL = "https://www.nseindia.com"
NSE_API_BASE = "https://www.nseindia.com/api"
PRE_MARKET_URL = NSE_API_BASE + "/market-data-pre-open?key=FO"
OPTION_CHAIN_INDEX_URL = NSE_API_BASE + "/option-chain-indices?symbol="
OPTION_CHAIN_EQUITY_URL = NSE_API_BASE + "/option-chain-equities?symbol="
INDICES_URL = NSE_API_BASE + "/allIndices"

MAX_RETRIES = 3
INITIAL_BACKOFF_S = 1.0
BACKOFF_MULTIPLIER = 2.0

USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
)

INDEX_SYMBOLS = {"NIFTY", "BANKNIFTY", "FINNIFTY", "MIDCPNIFTY", "NIFTY 50", "NIFTY BANK"}


def _build_headers() -> dict:
    return {
        "User-Agent": USER_AGENT,
        "Accept": "application/json, text/plain, */*",
        "Accept-Language": "en-US,en;q=0.9",
        "Accept-Encoding": "gzip, deflate, br",
        "Referer": "https://www.nseindia.com/",
        "Connection": "keep-alive",
        "Cache-Control": "no-cache",
        "Pragma": "no-cache",
    }


def _warm_up_session(session: requests.Session) -> None:
    """Hit the NSE base URL to establish session cookies."""
    try:
        session.get(NSE_BASE_URL, headers=_build_headers(), timeout=10)
    except Exception as e:
        logger.debug("Session warm-up completed (errors expected): %s", e)


def _execute_with_retry(url: str) -> dict | None:
    """Execute GET request to NSE with retry logic and exponential backoff."""
    backoff = INITIAL_BACKOFF_S
    session = requests.Session()

    for attempt in range(1, MAX_RETRIES + 1):
        try:
            if attempt == 1:
                _warm_up_session(session)

            response = session.get(url, headers=_build_headers(), timeout=15)
            if response.ok:
                logger.debug("Fetched data from %s on attempt %d", url, attempt)
                return response.json()

            logger.warning("Non-2xx from %s on attempt %d: status=%d", url, attempt, response.status_code)

        except requests.RequestException as e:
            logger.warning("Request to %s failed on attempt %d/%d: %s", url, attempt, MAX_RETRIES, e)
        except Exception as e:
            logger.error("Unexpected error fetching %s on attempt %d/%d: %s", url, attempt, MAX_RETRIES, e)

        if attempt < MAX_RETRIES:
            logger.info("Retrying in %.0f ms (attempt %d/%d)", backoff * 1000, attempt + 1, MAX_RETRIES)
            time.sleep(backoff)
            backoff *= BACKOFF_MULTIPLIER

    logger.error("All %d retry attempts exhausted for URL: %s", MAX_RETRIES, url)
    return None


def fetch_pre_market_data(symbol: str) -> dict:
    """Fetches pre-market data for a given symbol from NSE."""
    logger.info("Fetching pre-market data for symbol: %s", symbol)
    response = _execute_with_retry(PRE_MARKET_URL)
    if response is None:
        logger.warning("No pre-market data returned for symbol: %s", symbol)
        return {}

    data = response.get("data", [])
    if isinstance(data, list):
        for entry in data:
            metadata = entry.get("metadata", {})
            if metadata.get("symbol", "").upper() == symbol.upper():
                return entry
    return response


def fetch_option_chain(symbol: str) -> dict:
    """Fetches the option chain for a given symbol from NSE."""
    logger.info("Fetching option chain for symbol: %s", symbol)
    if symbol.upper() in INDEX_SYMBOLS:
        url = OPTION_CHAIN_INDEX_URL + symbol
    else:
        url = OPTION_CHAIN_EQUITY_URL + symbol

    response = _execute_with_retry(url)
    if response is None:
        logger.warning("No option chain data returned for symbol: %s", symbol)
        return {}
    return response


def fetch_indices_data() -> dict:
    """Fetches data for all NSE indices."""
    logger.info("Fetching all indices data")
    response = _execute_with_retry(INDICES_URL)
    if response is None:
        logger.warning("No indices data returned")
        return {}
    return response
