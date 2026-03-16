"""Display formatters for Indian financial data.

Supports INR formatting with Indian numbering system (lakhs/crores),
percentage formatting, and PnL display.
"""


def format_inr(amount: float) -> str:
    """Formats amount in Indian numbering (lakhs/crores) with INR symbol.

    Examples:
        1234.56      -> "₹1,234.56"
        123456.78    -> "₹1,23,456.78"
        12345678.90  -> "₹1,23,45,678.90"
    """
    if amount < 0:
        return f"-₹{_indian_format(abs(amount))}"
    return f"₹{_indian_format(amount)}"


def _indian_format(n: float) -> str:
    parts = f"{n:.2f}".split(".")
    integer_part = parts[0]
    decimal_part = parts[1]

    if len(integer_part) <= 3:
        return f"{integer_part}.{decimal_part}"

    last_three = integer_part[-3:]
    remaining = integer_part[:-3]

    groups = []
    while remaining:
        groups.insert(0, remaining[-2:] if len(remaining) >= 2 else remaining)
        remaining = remaining[:-2]

    return ",".join(groups) + "," + last_three + "." + decimal_part


def format_percent(value: float) -> str:
    """Formats value as percentage with sign. Input is decimal (0.05 = 5%)."""
    sign = "+" if value >= 0 else ""
    return f"{sign}{value * 100:.2f}%"


def format_pnl(amount: float) -> str:
    """Formats PnL with sign and INR."""
    sign = "+" if amount >= 0 else ""
    return f"{sign}{format_inr(amount)}"
