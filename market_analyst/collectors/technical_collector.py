"""Collects technical data including volume profiles and VWAP calculations.

In production, this would integrate with a market data provider API
(e.g., Kite Connect, Angel Broking SmartAPI) for real-time/historical data.
"""

import logging
from dataclasses import dataclass

logger = logging.getLogger(__name__)


@dataclass
class VolumeData:
    symbol: str = ""
    current_volume: int = 0
    average_volume_20d: int = 0
    average_volume_5d: int = 0
    delivery_percent: float = 0.0
    previous_day_volume: int = 0


@dataclass
class VwapData:
    symbol: str = ""
    vwap: float = 0.0
    previous_day_vwap: float = 0.0
    current_price: float = 0.0
    vwap_deviation: float = 0.0


def collect_volume_data(symbol: str) -> VolumeData:
    """Collects volume data for a given symbol.

    In production: call broker API for real-time volume data.
    For now, returns stub data.
    """
    logger.info("Collecting volume data for: %s", symbol)
    return VolumeData(symbol=symbol)


def collect_vwap_data(symbol: str) -> VwapData:
    """Collects VWAP-related data for a symbol.

    In production: calculate VWAP from tick data or broker API.
    For now, returns stub data.
    """
    logger.info("Collecting VWAP data for: %s", symbol)
    return VwapData(symbol=symbol)


def calculate_volume_ratio(current_volume: int, average_volume: int) -> float:
    """Volume ratio = current / average.

    > 1.5 indicates heightened interest; > 2.5 indicates unusual activity.
    """
    if average_volume <= 0:
        return 0.0
    return current_volume / average_volume


def calculate_vwap_position(current_price: float, vwap: float) -> float:
    """VWAP position as % deviation from VWAP.

    Positive = price above VWAP (bullish intraday), negative = below (bearish).
    """
    if vwap <= 0:
        return 0.0
    return ((current_price - vwap) / vwap) * 100.0
