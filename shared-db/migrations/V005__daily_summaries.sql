CREATE TABLE daily_summaries (
    id SERIAL PRIMARY KEY,
    trade_date DATE UNIQUE,
    total_trades INTEGER,
    winning_trades INTEGER,
    losing_trades INTEGER,
    win_rate DOUBLE PRECISION,
    total_pnl DOUBLE PRECISION,
    profit_factor DOUBLE PRECISION,
    max_drawdown DOUBLE PRECISION,
    sharpe_ratio DOUBLE PRECISION,
    market_regime VARCHAR(255),
    daily_narrative TEXT,
    trade_postmortems JSONB,
    signals_shown INTEGER,
    signals_approved INTEGER,
    signals_skipped INTEGER,
    approval_accuracy DOUBLE PRECISION,
    skip_accuracy DOUBLE PRECISION,
    override_impact JSONB,
    decision_narrative TEXT,
    missed_opportunities JSONB,
    patterns_identified JSONB,
    recommended_adjustments JSONB,
    created_at TIMESTAMP
);

CREATE INDEX idx_daily_summaries_trade_date ON daily_summaries(trade_date);
