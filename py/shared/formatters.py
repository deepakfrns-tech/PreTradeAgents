"""Indian financial formatting utilities."""


def format_inr(amount: float) -> str:
    """Format amount in Indian numbering (lakhs/crores) with ₹ symbol."""
    if amount < 0:
        return f"-₹{_indian_format(abs(amount))}"
    return f"₹{_indian_format(amount)}"


def _indian_format(n: float) -> str:
    s = f"{n:,.2f}"
    # Convert Western grouping to Indian grouping
    parts = s.split(".")
    integer_part = parts[0].replace(",", "")
    decimal_part = parts[1] if len(parts) > 1 else "00"

    if len(integer_part) <= 3:
        return f"{integer_part}.{decimal_part}"

    last_three = integer_part[-3:]
    remaining = integer_part[:-3]
    # Group remaining digits in pairs from right
    groups = []
    while remaining:
        groups.insert(0, remaining[-2:] if len(remaining) >= 2 else remaining)
        remaining = remaining[:-2]
    return ",".join(groups) + "," + last_three + "." + decimal_part


def format_percent(value: float) -> str:
    """Format as percentage with sign."""
    sign = "+" if value >= 0 else ""
    return f"{sign}{value * 100:.2f}%"


def format_pnl(amount: float) -> str:
    """Format PnL with sign and INR."""
    sign = "+" if amount >= 0 else ""
    return f"{sign}{format_inr(amount)}"
