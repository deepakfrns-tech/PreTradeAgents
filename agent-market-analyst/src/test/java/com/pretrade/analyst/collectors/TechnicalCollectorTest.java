package com.pretrade.analyst.collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TechnicalCollectorTest {

    // --- calculateVolumeRatio tests ---

    @Test
    void calculateVolumeRatio_normalCase() {
        double ratio = TechnicalCollector.calculateVolumeRatio(200000, 100000);
        assertEquals(2.0, ratio, 0.001);
    }

    @Test
    void calculateVolumeRatio_belowAverage() {
        double ratio = TechnicalCollector.calculateVolumeRatio(50000, 100000);
        assertEquals(0.5, ratio, 0.001);
    }

    @Test
    void calculateVolumeRatio_zeroAverage() {
        double ratio = TechnicalCollector.calculateVolumeRatio(100000, 0);
        assertEquals(0.0, ratio, 0.001);
    }

    @Test
    void calculateVolumeRatio_negativeAverage() {
        double ratio = TechnicalCollector.calculateVolumeRatio(100000, -1);
        assertEquals(0.0, ratio, 0.001);
    }

    @Test
    void calculateVolumeRatio_equalVolumes() {
        double ratio = TechnicalCollector.calculateVolumeRatio(100000, 100000);
        assertEquals(1.0, ratio, 0.001);
    }

    // --- calculateVwapPosition tests ---

    @Test
    void calculateVwapPosition_priceAboveVwap() {
        double position = TechnicalCollector.calculateVwapPosition(105.0, 100.0);
        assertEquals(5.0, position, 0.001);
    }

    @Test
    void calculateVwapPosition_priceBelowVwap() {
        double position = TechnicalCollector.calculateVwapPosition(95.0, 100.0);
        assertEquals(-5.0, position, 0.001);
    }

    @Test
    void calculateVwapPosition_priceAtVwap() {
        double position = TechnicalCollector.calculateVwapPosition(100.0, 100.0);
        assertEquals(0.0, position, 0.001);
    }

    @Test
    void calculateVwapPosition_zeroVwap() {
        double position = TechnicalCollector.calculateVwapPosition(100.0, 0.0);
        assertEquals(0.0, position, 0.001);
    }

    @Test
    void calculateVwapPosition_negativeVwap() {
        double position = TechnicalCollector.calculateVwapPosition(100.0, -1.0);
        assertEquals(0.0, position, 0.001);
    }

    // --- DTO tests ---

    @Test
    void volumeData_builder_works() {
        TechnicalCollector.VolumeData data = TechnicalCollector.VolumeData.builder()
                .symbol("RELIANCE")
                .currentVolume(500000L)
                .averageVolume20D(300000L)
                .averageVolume5D(400000L)
                .deliveryPercent(45.5)
                .previousDayVolume(350000L)
                .build();

        assertEquals("RELIANCE", data.getSymbol());
        assertEquals(500000L, data.getCurrentVolume());
        assertEquals(45.5, data.getDeliveryPercent());
    }

    @Test
    void vwapData_builder_works() {
        TechnicalCollector.VwapData data = TechnicalCollector.VwapData.builder()
                .symbol("TCS")
                .vwap(3450.0)
                .previousDayVwap(3420.0)
                .currentPrice(3460.0)
                .vwapDeviation(0.29)
                .build();

        assertEquals("TCS", data.getSymbol());
        assertEquals(3450.0, data.getVwap());
        assertEquals(3460.0, data.getCurrentPrice());
    }
}
