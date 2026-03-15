package com.pretrade.analyst.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalystSettingsTest {

    @Test
    void defaults_areCorrect() {
        AnalystSettings settings = new AnalystSettings();
        assertEquals(0.5, settings.getGapThreshold());
        assertEquals(60.0, settings.getMinCompositeScore());
        assertEquals(20, settings.getTopNStocks());
        assertEquals(30, settings.getDataSourceTimeoutSeconds());
    }

    @Test
    void weights_defaultsAreCorrect() {
        AnalystSettings settings = new AnalystSettings();
        AnalystSettings.Weights weights = settings.getWeights();

        assertEquals(25, weights.getGap());
        assertEquals(20, weights.getSentiment());
        assertEquals(20, weights.getVolume());
        assertEquals(20, weights.getOi());
        assertEquals(15, weights.getAlignment());
    }

    @Test
    void weights_totalSumTo100() {
        AnalystSettings settings = new AnalystSettings();
        assertEquals(100, settings.getWeights().total());
    }

    @Test
    void setters_work() {
        AnalystSettings settings = new AnalystSettings();
        settings.setGapThreshold(1.0);
        settings.setMinCompositeScore(70.0);
        settings.setTopNStocks(10);
        settings.setDataSourceTimeoutSeconds(60);

        assertEquals(1.0, settings.getGapThreshold());
        assertEquals(70.0, settings.getMinCompositeScore());
        assertEquals(10, settings.getTopNStocks());
        assertEquals(60, settings.getDataSourceTimeoutSeconds());
    }

    @Test
    void weights_setters_work() {
        AnalystSettings.Weights weights = new AnalystSettings.Weights();
        weights.setGap(30);
        weights.setSentiment(25);
        weights.setVolume(20);
        weights.setOi(15);
        weights.setAlignment(10);

        assertEquals(30, weights.getGap());
        assertEquals(25, weights.getSentiment());
        assertEquals(100, weights.total());
    }
}
