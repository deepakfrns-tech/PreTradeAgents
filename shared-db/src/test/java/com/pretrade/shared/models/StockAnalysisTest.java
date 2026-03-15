package com.pretrade.shared.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class StockAnalysisTest {

    @Test
    void builder_createsValidInstance() {
        StockAnalysis analysis = StockAnalysis.builder()
                .tradeDate(LocalDate.of(2026, 3, 15))
                .symbol("RELIANCE")
                .gapPercent(1.5)
                .gapDirection("UP")
                .gapCategory("SMALL")
                .gapScore(75.0)
                .sentimentScore(0.8)
                .sentimentLevel("BULLISH")
                .compositeScore(82.5)
                .signalDirection("BULLISH")
                .recommendedAction("BUY_CE")
                .entryStrike(2900.0)
                .estimatedPremium(45.0)
                .stopLoss(22.5)
                .target(90.0)
                .riskRewardRatio(2.0)
                .build();

        assertEquals("RELIANCE", analysis.getSymbol());
        assertEquals(LocalDate.of(2026, 3, 15), analysis.getTradeDate());
        assertEquals(1.5, analysis.getGapPercent());
        assertEquals("UP", analysis.getGapDirection());
        assertEquals(82.5, analysis.getCompositeScore());
        assertEquals("BULLISH", analysis.getSignalDirection());
        assertEquals(2.0, analysis.getRiskRewardRatio());
    }

    @Test
    void noArgsConstructor_createsInstance() {
        StockAnalysis analysis = new StockAnalysis();
        assertNotNull(analysis);
        assertNull(analysis.getId());
        assertNull(analysis.getSymbol());
    }

    @Test
    void settersAndGetters_work() {
        StockAnalysis analysis = new StockAnalysis();
        analysis.setSymbol("TCS");
        analysis.setTradeDate(LocalDate.of(2026, 3, 14));
        analysis.setGapPercent(2.3);
        analysis.setCompositeScore(91.0);
        analysis.setClaudeReasoning("Strong gap up with high volume and bullish sentiment");
        analysis.setRiskWarnings("High VIX environment");

        assertEquals("TCS", analysis.getSymbol());
        assertEquals(2.3, analysis.getGapPercent());
        assertEquals(91.0, analysis.getCompositeScore());
        assertNotNull(analysis.getClaudeReasoning());
        assertNotNull(analysis.getRiskWarnings());
    }

    @Test
    void allFieldsAreSettable() {
        StockAnalysis analysis = StockAnalysis.builder()
                .id(1L)
                .tradeDate(LocalDate.now())
                .symbol("INFY")
                .gapPercent(0.5)
                .gapDirection("DOWN")
                .atrRatio(1.2)
                .gapCategory("SMALL")
                .gapScore(30.0)
                .sentimentScore(-0.5)
                .sentimentLevel("BEARISH")
                .sentimentReasoning("Negative earnings")
                .headlineDetails("{}")
                .volumeRatio(2.5)
                .volumeLevel("HIGH")
                .vwapPosition(-1.5)
                .volumeScore(80.0)
                .pcr(0.7)
                .oiBuildup("SHORT_BUILDUP")
                .maxPain(1500.0)
                .ivPercentile(75.0)
                .suggestedStrike(1480.0)
                .oiScore(65.0)
                .compositeScore(55.0)
                .signalDirection("BEARISH")
                .recommendedAction("BUY_PE")
                .claudeReasoning("Bearish setup")
                .riskWarnings("Earnings day volatility")
                .confidenceLevel("MEDIUM")
                .entryStrike(1480.0)
                .estimatedPremium(35.0)
                .stopLoss(17.5)
                .target(70.0)
                .riskRewardRatio(2.0)
                .build();

        assertEquals(1L, analysis.getId());
        assertEquals("INFY", analysis.getSymbol());
        assertEquals("SHORT_BUILDUP", analysis.getOiBuildup());
        assertEquals("MEDIUM", analysis.getConfidenceLevel());
    }

    @Test
    void equals_and_hashCode_work() {
        StockAnalysis a1 = StockAnalysis.builder().id(1L).symbol("RELIANCE").build();
        StockAnalysis a2 = StockAnalysis.builder().id(1L).symbol("RELIANCE").build();
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void toString_doesNotThrow() {
        StockAnalysis analysis = StockAnalysis.builder()
                .symbol("NIFTY")
                .compositeScore(88.0)
                .build();
        assertNotNull(analysis.toString());
        assertTrue(analysis.toString().contains("NIFTY"));
    }
}
