package com.pretrade.analyst.collectors;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NseCollectorTest {

    // --- PreMarketEntry DTO tests ---

    @Test
    void preMarketEntry_builder_works() {
        NseCollector.PreMarketEntry entry = NseCollector.PreMarketEntry.builder()
                .symbol("RELIANCE")
                .previousClose(2800.0)
                .iep(2842.0)
                .change(42.0)
                .changePercent(1.5)
                .finalQuantity(150000L)
                .totalBuyQuantity(100000L)
                .totalSellQuantity(50000L)
                .lastPrice(2840.0)
                .yearHigh(3000.0)
                .yearLow(2200.0)
                .build();

        assertEquals("RELIANCE", entry.getSymbol());
        assertEquals(2800.0, entry.getPreviousClose());
        assertEquals(2842.0, entry.getIep());
    }

    @Test
    void preMarketEntry_getGapPercent_calculatesCorrectly() {
        NseCollector.PreMarketEntry entry = NseCollector.PreMarketEntry.builder()
                .symbol("TCS")
                .previousClose(1000.0)
                .iep(1015.0)
                .build();

        assertEquals(1.5, entry.getGapPercent(), 0.001);
    }

    @Test
    void preMarketEntry_getGapPercent_negativeGap() {
        NseCollector.PreMarketEntry entry = NseCollector.PreMarketEntry.builder()
                .symbol("INFY")
                .previousClose(1000.0)
                .iep(980.0)
                .build();

        assertEquals(-2.0, entry.getGapPercent(), 0.001);
    }

    @Test
    void preMarketEntry_getGapPercent_zeroPreviousClose() {
        NseCollector.PreMarketEntry entry = NseCollector.PreMarketEntry.builder()
                .symbol("TEST")
                .previousClose(0.0)
                .iep(100.0)
                .build();

        assertEquals(0.0, entry.getGapPercent());
    }

    // --- OptionChainData DTO tests ---

    @Test
    void optionChainData_builder_works() {
        NseCollector.OptionChainData data = NseCollector.OptionChainData.builder()
                .symbol("NIFTY")
                .underlyingValue(22500.0)
                .expiryDates(List.of("2026-03-19", "2026-03-26"))
                .entries(new ArrayList<>())
                .build();

        assertEquals("NIFTY", data.getSymbol());
        assertEquals(22500.0, data.getUnderlyingValue());
        assertEquals(2, data.getExpiryDates().size());
        assertTrue(data.getEntries().isEmpty());
    }

    // --- OptionEntry DTO tests ---

    @Test
    void optionEntry_settersAndGetters_work() {
        NseCollector.OptionEntry entry = new NseCollector.OptionEntry();
        entry.setStrikePrice(22500.0);
        entry.setExpiryDate("2026-03-19");
        entry.setCallOI(5000000L);
        entry.setCallChangeInOI(250000L);
        entry.setCallLTP(150.0);
        entry.setCallIV(12.5);
        entry.setCallVolume(100000L);
        entry.setPutOI(3000000L);
        entry.setPutChangeInOI(-100000L);
        entry.setPutLTP(120.0);
        entry.setPutIV(13.0);
        entry.setPutVolume(80000L);

        assertEquals(22500.0, entry.getStrikePrice());
        assertEquals(5000000L, entry.getCallOI());
        assertEquals(3000000L, entry.getPutOI());
        assertEquals(12.5, entry.getCallIV());
    }

    // --- MarketSnapshotData DTO tests ---

    @Test
    void marketSnapshotData_builder_works() {
        NseCollector.MarketSnapshotData snapshot = NseCollector.MarketSnapshotData.builder()
                .niftyValue(22500.0)
                .niftyChange(0.75)
                .bankNiftyValue(48000.0)
                .bankNiftyChange(1.2)
                .indiaVix(14.5)
                .advances(35)
                .declines(15)
                .advanceDeclineRatio(2.33)
                .build();

        assertEquals(22500.0, snapshot.getNiftyValue());
        assertEquals(48000.0, snapshot.getBankNiftyValue());
        assertEquals(14.5, snapshot.getIndiaVix());
        assertEquals(35, snapshot.getAdvances());
        assertEquals(15, snapshot.getDeclines());
    }

    @Test
    void marketSnapshotData_emptyBuilder_works() {
        NseCollector.MarketSnapshotData snapshot = NseCollector.MarketSnapshotData.builder().build();
        assertEquals(0.0, snapshot.getNiftyValue());
        assertEquals(0, snapshot.getAdvances());
    }
}
