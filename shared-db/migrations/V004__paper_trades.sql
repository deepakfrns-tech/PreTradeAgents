CREATE TABLE paper_trades (
    id SERIAL PRIMARY KEY,
    trade_date DATE,
    symbol VARCHAR(255),
    signal_id INTEGER REFERENCES stock_analysis(id),
    direction VARCHAR(255),
    strike_price DOUBLE PRECISION,
    entry_price DOUBLE PRECISION,
    entry_time TIMESTAMP,
    lot_size INTEGER,
    entry_reasoning TEXT,
    exit_price DOUBLE PRECISION,
    exit_time TIMESTAMP,
    exit_reason VARCHAR(255),
    exit_reasoning TEXT,
    pnl_amount DOUBLE PRECISION,
    pnl_percent DOUBLE PRECISION,
    max_profit DOUBLE PRECISION,
    max_drawdown DOUBLE PRECISION,
    price_trail JSONB,
    sl_adjustments JSONB,
    status VARCHAR(255),
    created_at TIMESTAMP
);

CREATE INDEX idx_paper_trades_trade_date ON paper_trades(trade_date);
CREATE INDEX idx_paper_trades_symbol ON paper_trades(symbol);
CREATE INDEX idx_paper_trades_status ON paper_trades(status);
