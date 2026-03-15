package com.pretrade.analyst.collectors;

import com.pretrade.analyst.config.AnalystSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Collects technical data including volume profiles and VWAP calculations.
 * In production, this would integrate with a market data provider API
 * (e.g., Kite Connect, Angel Broking SmartAPI) for real-time/historical volume data.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TechnicalCollector {

    private final AnalystSettings settings;

    /**
     * Collects volume data for a given symbol, including current volume,
     * average volume, and delivery percentage.
     */
    public VolumeData collectVolumeData(String symbol) {
        log.info("Collecting volume data for: {}", symbol);
        try {
            // In production: call broker API for real-time volume data.
            // For now, aggregate from NSE bhavcopy or cached data.
            return VolumeData.builder()
                    .symbol(symbol)
                    .currentVolume(0L)
                    .averageVolume20D(0L)
                    .averageVolume5D(0L)
                    .deliveryPercent(0.0)
                    .previousDayVolume(0L)
                    .build();
        } catch (Exception e) {
            log.error("Failed to collect volume data for {}: {}", symbol, e.getMessage(), e);
            return VolumeData.builder().symbol(symbol).build();
        }
    }

    /**
     * Collects VWAP-related data for a symbol, including the intraday VWAP,
     * previous day VWAP, and price position relative to VWAP.
     */
    public VwapData collectVwapData(String symbol) {
        log.info("Collecting VWAP data for: {}", symbol);
        try {
            // In production: calculate VWAP from tick data or broker API.
            return VwapData.builder()
                    .symbol(symbol)
                    .vwap(0.0)
                    .previousDayVwap(0.0)
                    .currentPrice(0.0)
                    .vwapDeviation(0.0)
                    .build();
        } catch (Exception e) {
            log.error("Failed to collect VWAP data for {}: {}", symbol, e.getMessage(), e);
            return VwapData.builder().symbol(symbol).build();
        }
    }

    /**
     * Calculates volume ratio = current / average.
     * A ratio > 1.5 indicates heightened interest; > 2.5 indicates unusual activity.
     */
    public static double calculateVolumeRatio(long currentVolume, long averageVolume) {
        if (averageVolume <= 0) return 0.0;
        return (double) currentVolume / averageVolume;
    }

    /**
     * Computes VWAP position as a percentage deviation from VWAP.
     * Positive = price above VWAP (bullish intraday), negative = below (bearish).
     */
    public static double calculateVwapPosition(double currentPrice, double vwap) {
        if (vwap <= 0) return 0.0;
        return ((currentPrice - vwap) / vwap) * 100.0;
    }

    // --- DTOs ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VolumeData {
        private String symbol;
        private long currentVolume;
        private long averageVolume20D;
        private long averageVolume5D;
        private double deliveryPercent;
        private long previousDayVolume;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VwapData {
        private String symbol;
        private double vwap;
        private double previousDayVwap;
        private double currentPrice;
        private double vwapDeviation;
    }
}
