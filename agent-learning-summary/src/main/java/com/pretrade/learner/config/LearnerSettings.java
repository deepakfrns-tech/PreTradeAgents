package com.pretrade.learner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "learner")
public class LearnerSettings {

    /** Minimum confidence score (0-1) for a trade to be included in learning */
    private double minConfidenceForLearning = 0.7;

    /** Number of past days to look back when mining patterns */
    private int lookbackDays = 30;

    /** Minimum times a pattern must appear before it's recorded as a strategy learning */
    private int patternMinOccurrences = 3;
}
