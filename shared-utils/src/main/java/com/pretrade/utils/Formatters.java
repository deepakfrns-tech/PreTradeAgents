package com.pretrade.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Display formatters for Indian financial data.
 * Supports INR formatting with Indian numbering system (lakhs/crores),
 * percentage formatting, and PnL display.
 */
@Slf4j
@Component
public class Formatters {

    private static final String INR_SYMBOL = "\u20B9";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    /**
     * Formats a double value as Indian Rupees with the Indian numbering system.
     * Uses lakhs (1,00,000) and crores (1,00,00,000) grouping.
     * Examples:
     *   1234.56      -> "₹1,234.56"
     *   123456.78    -> "₹1,23,456.78"
     *   12345678.90  -> "₹1,23,45,678.90"
     *
     * @param amount the amount to format
     * @return formatted INR string
     */
    public String formatINR(double amount) {
        boolean negative = amount < 0;
        double absAmount = Math.abs(amount);

        // Split into integer and decimal parts
        long integerPart = (long) absAmount;
        String decimalPart = String.format("%.2f", absAmount - integerPart).substring(1); // ".XX"

        String intStr = Long.toString(integerPart);
        String formattedInt;

        if (intStr.length() <= 3) {
            formattedInt = intStr;
        } else {
            // Last 3 digits
            String lastThree = intStr.substring(intStr.length() - 3);
            String remaining = intStr.substring(0, intStr.length() - 3);

            // Group remaining digits in pairs (Indian numbering)
            StringBuilder sb = new StringBuilder();
            int len = remaining.length();
            for (int i = 0; i < len; i++) {
                if (i > 0 && (len - i) % 2 == 0) {
                    sb.append(',');
                }
                sb.append(remaining.charAt(i));
            }
            sb.append(',').append(lastThree);
            formattedInt = sb.toString();
        }

        String result = INR_SYMBOL + formattedInt + decimalPart;
        return negative ? "-" + result : result;
    }

    /**
     * Formats a double value as a percentage with 2 decimal places.
     * Example: 0.0534 -> "+5.34%", -0.12 -> "-12.00%"
     *
     * @param value the decimal value (e.g., 0.05 for 5%)
     * @return formatted percentage string with sign
     */
    public String formatPercent(double value) {
        double percent = value * 100;
        String sign = percent >= 0 ? "+" : "";
        return String.format("%s%.2f%%", sign, percent);
    }

    /**
     * Formats a PnL (Profit and Loss) value in INR with color-indicating prefix.
     * Positive values are prefixed with "+", negative with "-".
     * Example: 15000.50 -> "+₹15,000.50", -5000.25 -> "-₹5,000.25"
     *
     * @param pnl the profit/loss amount
     * @return formatted PnL string
     */
    public String formatPnL(double pnl) {
        if (pnl >= 0) {
            return "+" + formatINR(pnl);
        } else {
            return formatINR(pnl);
        }
    }

    /**
     * Formats a LocalDateTime as a human-readable timestamp string.
     * Format: "dd-MMM-yyyy HH:mm:ss" (e.g., "14-Mar-2026 09:15:00")
     *
     * @param dateTime the LocalDateTime to format
     * @return formatted timestamp string
     */
    public String formatTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(TIMESTAMP_FORMATTER);
    }
}
