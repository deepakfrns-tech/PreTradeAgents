package com.pretrade.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FormattersTest {

    private Formatters formatters;

    @BeforeEach
    void setUp() {
        formatters = new Formatters();
    }

    // --- formatINR tests ---

    @Test
    void formatINR_smallAmount() {
        String result = formatters.formatINR(123.45);
        assertEquals("\u20B9123.45", result);
    }

    @Test
    void formatINR_thousandsRange() {
        String result = formatters.formatINR(1234.56);
        assertEquals("\u20B91,234.56", result);
    }

    @Test
    void formatINR_lakhsRange() {
        String result = formatters.formatINR(123456.78);
        assertEquals("\u20B91,23,456.78", result);
    }

    @Test
    void formatINR_croresRange() {
        String result = formatters.formatINR(12345678.90);
        assertEquals("\u20B91,23,45,678.90", result);
    }

    @Test
    void formatINR_negativeAmount() {
        String result = formatters.formatINR(-5000.00);
        assertTrue(result.startsWith("-\u20B9"));
        assertTrue(result.contains("5,000"));
    }

    @Test
    void formatINR_zero() {
        String result = formatters.formatINR(0.0);
        assertEquals("\u20B90.00", result);
    }

    // --- formatPercent tests ---

    @Test
    void formatPercent_positiveValue() {
        String result = formatters.formatPercent(0.0534);
        assertEquals("+5.34%", result);
    }

    @Test
    void formatPercent_negativeValue() {
        String result = formatters.formatPercent(-0.12);
        assertEquals("-12.00%", result);
    }

    @Test
    void formatPercent_zero() {
        String result = formatters.formatPercent(0.0);
        assertEquals("+0.00%", result);
    }

    // --- formatPnL tests ---

    @Test
    void formatPnL_positiveProfit() {
        String result = formatters.formatPnL(15000.50);
        assertTrue(result.startsWith("+\u20B9"));
        assertTrue(result.contains("15,000"));
    }

    @Test
    void formatPnL_negativeLoss() {
        String result = formatters.formatPnL(-5000.25);
        assertTrue(result.startsWith("-\u20B9"));
        assertTrue(result.contains("5,000"));
    }

    @Test
    void formatPnL_zero() {
        String result = formatters.formatPnL(0.0);
        assertTrue(result.startsWith("+\u20B9"));
    }

    // --- formatTimestamp tests ---

    @Test
    void formatTimestamp_validDateTime() {
        LocalDateTime dt = LocalDateTime.of(2026, 3, 14, 9, 15, 0);
        String result = formatters.formatTimestamp(dt);
        assertEquals("14-Mar-2026 09:15:00", result);
    }

    @Test
    void formatTimestamp_null() {
        String result = formatters.formatTimestamp(null);
        assertEquals("N/A", result);
    }
}
