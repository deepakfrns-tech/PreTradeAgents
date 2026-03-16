"""Tests for shared.formatters module."""

from shared.formatters import format_inr, format_percent, format_pnl


def test_format_inr_small_amount():
    assert format_inr(1234.56) == "₹1,234.56"


def test_format_inr_lakhs():
    assert format_inr(123456.78) == "₹1,23,456.78"


def test_format_inr_crores():
    assert format_inr(12345678.90) == "₹1,23,45,678.90"


def test_format_inr_negative():
    result = format_inr(-5000.25)
    assert result.startswith("-₹")
    assert "5,000.25" in result


def test_format_inr_zero():
    assert format_inr(0) == "₹0.00"


def test_format_inr_under_thousand():
    assert format_inr(999.99) == "₹999.99"


def test_format_percent_positive():
    assert format_percent(0.0534) == "+5.34%"


def test_format_percent_negative():
    assert format_percent(-0.12) == "-12.00%"


def test_format_percent_zero():
    assert format_percent(0) == "+0.00%"


def test_format_pnl_positive():
    result = format_pnl(15000.50)
    assert result.startswith("+₹")


def test_format_pnl_negative():
    result = format_pnl(-5000.25)
    assert result.startswith("-")
    assert "₹" in result
