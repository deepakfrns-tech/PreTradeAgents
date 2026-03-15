package com.pretrade.shared.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MarketSnapshotTest {

    @Test
    void builder_createsValidInstance() {
        MarketSnapshot snapshot = MarketSnapshot.builder()
                .tradeDate(LocalDate.of(2026, 3, 15))
                .timestamp(LocalDateTime.of(2026, 3, 15, 9, 0, 0))
                .niftyGapPercent(0.75)
                .marketSentiment("BULLISH")
                .indiaVix(14.5)
                .advanceDeclineRatio(1.8)
                .fiiDiiData("{\"fii\": 1500, \"dii\": -800}")
                .rawPreMarketData("{}")
                .build();

        assertEquals(LocalDate.of(2026, 3, 15), snapshot.getTradeDate());
        assertEquals(0.75, snapshot.getNiftyGapPercent());
        assertEquals("BULLISH", snapshot.getMarketSentiment());
        assertEquals(14.5, snapshot.getIndiaVix());
        assertEquals(1.8, snapshot.getAdvanceDeclineRatio());
    }

    @Test
    void noArgsConstructor_createsInstance() {
        MarketSnapshot snapshot = new MarketSnapshot();
        assertNotNull(snapshot);
        assertNull(snapshot.getId());
    }

    @Test
    void settersAndGetters_work() {
        MarketSnapshot snapshot = new MarketSnapshot();
        snapshot.setTradeDate(LocalDate.now());
        snapshot.setNiftyGapPercent(-0.5);
        snapshot.setMarketSentiment("BEARISH");
        snapshot.setIndiaVix(22.0);

        assertEquals(-0.5, snapshot.getNiftyGapPercent());
        assertEquals("BEARISH", snapshot.getMarketSentiment());
        assertEquals(22.0, snapshot.getIndiaVix());
    }

    @Test
    void equals_and_hashCode_work() {
        MarketSnapshot s1 = MarketSnapshot.builder().id(1L).tradeDate(LocalDate.now()).build();
        MarketSnapshot s2 = MarketSnapshot.builder().id(1L).tradeDate(LocalDate.now()).build();
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void jsonbFields_acceptStringJson() {
        MarketSnapshot snapshot = MarketSnapshot.builder()
                .fiiDiiData("{\"fii_net\": 2500.5, \"dii_net\": -1200.3}")
                .rawPreMarketData("[{\"symbol\": \"RELIANCE\", \"gap\": 1.5}]")
                .build();

        assertTrue(snapshot.getFiiDiiData().contains("fii_net"));
        assertTrue(snapshot.getRawPreMarketData().contains("RELIANCE"));
    }
}
