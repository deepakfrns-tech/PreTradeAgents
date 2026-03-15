package com.pretrade.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * Utility class for IST (Indian Standard Time) time operations
 * relevant to NSE market hours and expiry calculations.
 */
@Slf4j
@Component
public class TimeUtils {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    private static final LocalTime PRE_MARKET_OPEN = LocalTime.of(9, 0);
    private static final LocalTime MARKET_OPEN = LocalTime.of(9, 15);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(15, 30);

    /**
     * Returns the current date-time in IST.
     *
     * @return current LocalDateTime in Asia/Kolkata timezone
     */
    public LocalDateTime nowIST() {
        return LocalDateTime.now(IST_ZONE);
    }

    /**
     * Checks if the current time falls within the pre-market window (9:00 AM - 9:15 AM IST).
     *
     * @return true if currently in pre-market session
     */
    public boolean isPreMarketWindow() {
        LocalDateTime now = nowIST();
        LocalTime currentTime = now.toLocalTime();
        return isWeekday(now.toLocalDate())
                && !currentTime.isBefore(PRE_MARKET_OPEN)
                && currentTime.isBefore(MARKET_OPEN);
    }

    /**
     * Checks if the market is currently open (9:15 AM - 3:30 PM IST on weekdays).
     *
     * @return true if the market is currently in regular trading session
     */
    public boolean isMarketOpen() {
        LocalDateTime now = nowIST();
        LocalTime currentTime = now.toLocalTime();
        return isWeekday(now.toLocalDate())
                && !currentTime.isBefore(MARKET_OPEN)
                && !currentTime.isAfter(MARKET_CLOSE);
    }

    /**
     * Checks if the market is currently closed.
     * The market is closed outside 9:00 AM - 3:30 PM IST or on weekends.
     *
     * @return true if the market is closed
     */
    public boolean isMarketClosed() {
        return !isPreMarketWindow() && !isMarketOpen();
    }

    /**
     * Calculates the current weekly expiry date (Thursday).
     * If today is past Thursday, returns next Thursday.
     * If today is Thursday and market is closed, returns next Thursday.
     *
     * @return the current expiry date as LocalDate
     */
    public LocalDate getCurrentExpiry() {
        LocalDateTime now = nowIST();
        LocalDate today = now.toLocalDate();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        // Find this week's Thursday
        LocalDate thisThursday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY));

        // If today is Thursday and market is already closed, move to next Thursday
        if (dayOfWeek == DayOfWeek.THURSDAY && now.toLocalTime().isAfter(MARKET_CLOSE)) {
            return thisThursday.plusWeeks(1);
        }

        // If we've already passed Thursday this week, get next Thursday
        if (dayOfWeek.getValue() > DayOfWeek.THURSDAY.getValue()) {
            return today.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
        }

        return thisThursday;
    }

    /**
     * Calculates the next weekly expiry date (the Thursday after the current expiry).
     *
     * @return the next expiry date as LocalDate
     */
    public LocalDate getNextExpiry() {
        return getCurrentExpiry().plusWeeks(1);
    }

    /**
     * Converts an Instant to IST LocalDateTime.
     *
     * @param instant the Instant to convert
     * @return the corresponding LocalDateTime in IST
     */
    public LocalDateTime toIST(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, IST_ZONE);
    }

    /**
     * Checks if today (IST) is a weekday (Monday-Friday).
     *
     * @return true if today is a weekday
     */
    public boolean isWeekday() {
        return isWeekday(nowIST().toLocalDate());
    }

    /**
     * Checks if a given date is a weekday (Monday-Friday).
     *
     * @param date the date to check
     * @return true if the date is a weekday
     */
    private boolean isWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    /**
     * Returns the IST ZoneId for external use.
     *
     * @return the Asia/Kolkata ZoneId
     */
    public ZoneId getISTZone() {
        return IST_ZONE;
    }

    /**
     * Returns the current ZonedDateTime in IST.
     *
     * @return current ZonedDateTime in Asia/Kolkata timezone
     */
    public ZonedDateTime nowZonedIST() {
        return ZonedDateTime.now(IST_ZONE);
    }
}
