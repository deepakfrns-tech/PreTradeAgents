package com.pretrade.executor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "executor")
public class ExecutorSettings {

    /** Maximum number of open paper positions at any time */
    private int maxPositions = 5;

    /** Maximum loss allowed per individual trade (INR) */
    private double maxLossPerTrade = 2000.0;

    /** Maximum total loss allowed in a single trading day (INR) */
    private double maxDailyLoss = 10000.0;

    /** Interval in milliseconds for monitoring open positions */
    private long monitorIntervalMs = 5000L;

    /** Time to force-exit all positions at end of day (HH:mm, IST) */
    private String eodExitTime = "15:15";

    /** Trailing stop loss percentage relative to peak P&L */
    private double trailingStopPercent = 30.0;
}
