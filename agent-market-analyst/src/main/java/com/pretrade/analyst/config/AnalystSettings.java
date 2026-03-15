package com.pretrade.analyst.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "analyst")
public class AnalystSettings {

    /** Minimum gap percentage to consider a stock for analysis */
    private double gapThreshold = 0.5;

    /** Minimum composite score to qualify as a tradeable signal */
    private double minCompositeScore = 60.0;

    /** Scoring weights for each analysis factor (must sum to 100) */
    private Weights weights = new Weights();

    /** Number of top-scoring stocks to analyze in depth */
    private int topNStocks = 20;

    /** HTTP timeout for external data sources in seconds */
    private int dataSourceTimeoutSeconds = 30;

    @Data
    public static class Weights {
        private int gap = 25;
        private int sentiment = 20;
        private int volume = 20;
        private int oi = 20;
        private int alignment = 15;

        public int total() {
            return gap + sentiment + volume + oi + alignment;
        }
    }
}
