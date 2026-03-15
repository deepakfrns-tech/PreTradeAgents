package com.pretrade.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeUtilsTest {

    private TimeUtils timeUtils;

    @BeforeEach
    void setUp() {
        timeUtils = new TimeUtils();
    }

    @Test
    void nowIST_returnsNonNull() {
        LocalDateTime now = timeUtils.nowIST();
        assertNotNull(now);
    }

    @Test
    void getISTZone_returnsAsiaKolkata() {
        ZoneId zone = timeUtils.getISTZone();
        assertEquals(ZoneId.of("Asia/Kolkata"), zone);
    }

    @Test
    void nowZonedIST_returnsISTTimezone() {
        ZonedDateTime zoned = timeUtils.nowZonedIST();
        assertEquals(ZoneId.of("Asia/Kolkata"), zoned.getZone());
    }

    @Test
    void toIST_withNull_returnsNull() {
        assertNull(timeUtils.toIST(null));
    }

    @Test
    void toIST_withValidInstant_returnsLocalDateTime() {
        Instant instant = Instant.parse("2026-03-15T03:45:00Z"); // 9:15 AM IST
        LocalDateTime result = timeUtils.toIST(instant);
        assertNotNull(result);
        assertEquals(9, result.getHour());
        assertEquals(15, result.getMinute());
    }

    @Test
    void getCurrentExpiry_returnsThursday() {
        LocalDate expiry = timeUtils.getCurrentExpiry();
        assertNotNull(expiry);
        assertEquals(DayOfWeek.THURSDAY, expiry.getDayOfWeek());
    }

    @Test
    void getNextExpiry_isOneWeekAfterCurrentExpiry() {
        LocalDate current = timeUtils.getCurrentExpiry();
        LocalDate next = timeUtils.getNextExpiry();
        assertEquals(current.plusWeeks(1), next);
    }

    @Test
    void getNextExpiry_returnsThursday() {
        LocalDate next = timeUtils.getNextExpiry();
        assertEquals(DayOfWeek.THURSDAY, next.getDayOfWeek());
    }

    @Test
    void getCurrentExpiry_isNotInThePast() {
        LocalDate expiry = timeUtils.getCurrentExpiry();
        LocalDate today = timeUtils.nowIST().toLocalDate();
        // Expiry should be today or in the future
        assertFalse(expiry.isBefore(today));
    }

    @Test
    void isMarketClosed_isInverseOfPreMarketAndMarketOpen() {
        // If market is closed, neither pre-market nor market-open should be true
        if (timeUtils.isMarketClosed()) {
            assertFalse(timeUtils.isPreMarketWindow());
            assertFalse(timeUtils.isMarketOpen());
        }
    }

    @Test
    void preMarketAndMarketOpen_areMutuallyExclusive() {
        // Pre-market and market-open should never both be true
        assertFalse(timeUtils.isPreMarketWindow() && timeUtils.isMarketOpen());
    }

    @Test
    void isWeekday_returnsBooleanWithoutError() {
        // Just verifying it doesn't throw
        boolean result = timeUtils.isWeekday();
        // Result depends on current day, so just check it returns
        assertNotNull(result);
    }
}
