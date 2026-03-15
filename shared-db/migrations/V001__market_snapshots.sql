CREATE TABLE market_snapshots (
    id SERIAL PRIMARY KEY,
    trade_date DATE,
    timestamp TIMESTAMP,
    nifty_gap_percent DOUBLE PRECISION,
    market_sentiment VARCHAR(255),
    india_vix DOUBLE PRECISION,
    advance_decline_ratio DOUBLE PRECISION,
    fii_dii_data JSONB,
    raw_pre_market_data JSONB
);

CREATE INDEX idx_market_snapshots_trade_date ON market_snapshots(trade_date);
