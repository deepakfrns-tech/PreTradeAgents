"""Tests for shared.lot_sizes module."""

from shared.lot_sizes import get_lot_size, is_valid_fno_stock, get_all_symbols, get_all_lot_sizes


def test_nifty_lot_size():
    assert get_lot_size("NIFTY") == 50


def test_banknifty_lot_size():
    assert get_lot_size("BANKNIFTY") == 15


def test_reliance_lot_size():
    assert get_lot_size("RELIANCE") == 250


def test_case_insensitive():
    assert get_lot_size("nifty") == 50
    assert get_lot_size("Reliance") == 250


def test_invalid_symbol_returns_minus_one():
    assert get_lot_size("INVALIDXYZ") == -1


def test_none_symbol_returns_minus_one():
    assert get_lot_size(None) == -1


def test_is_valid_fno_stock_true():
    assert is_valid_fno_stock("NIFTY") is True
    assert is_valid_fno_stock("RELIANCE") is True
    assert is_valid_fno_stock("TCS") is True


def test_is_valid_fno_stock_false():
    assert is_valid_fno_stock("INVALIDXYZ") is False


def test_is_valid_fno_stock_none():
    assert is_valid_fno_stock(None) is False


def test_get_all_symbols_returns_set():
    symbols = get_all_symbols()
    assert isinstance(symbols, set)
    assert "NIFTY" in symbols
    assert "BANKNIFTY" in symbols
    assert len(symbols) >= 50


def test_get_all_lot_sizes_returns_dict():
    lot_sizes = get_all_lot_sizes()
    assert isinstance(lot_sizes, dict)
    assert lot_sizes["NIFTY"] == 50
