"""Tests for market_analyst.collectors.technical_collector module."""

from market_analyst.collectors.technical_collector import (
    calculate_volume_ratio, calculate_vwap_position,
    collect_volume_data, collect_vwap_data,
)


def test_calculate_volume_ratio():
    assert calculate_volume_ratio(300, 200) == 1.5


def test_calculate_volume_ratio_zero_average():
    assert calculate_volume_ratio(100, 0) == 0.0


def test_calculate_vwap_position_above():
    result = calculate_vwap_position(105, 100)
    assert abs(result - 5.0) < 0.001


def test_calculate_vwap_position_below():
    result = calculate_vwap_position(95, 100)
    assert abs(result - (-5.0)) < 0.001


def test_calculate_vwap_position_zero_vwap():
    assert calculate_vwap_position(100, 0) == 0.0


def test_collect_volume_data_returns_stub():
    data = collect_volume_data("RELIANCE")
    assert data.symbol == "RELIANCE"
    assert data.current_volume == 0


def test_collect_vwap_data_returns_stub():
    data = collect_vwap_data("RELIANCE")
    assert data.symbol == "RELIANCE"
    assert data.vwap == 0.0
