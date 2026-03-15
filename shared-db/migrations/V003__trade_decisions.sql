CREATE TABLE trade_decisions (
    id SERIAL PRIMARY KEY,
    trade_date DATE,
    signal_id INTEGER REFERENCES stock_analysis(id),
    symbol VARCHAR(255),
    decision VARCHAR(255),
    decision_time TIMESTAMP,
    decision_reason TEXT,
    override_strike DOUBLE PRECISION,
    override_sl DOUBLE PRECISION,
    override_target DOUBLE PRECISION,
    override_lot_size INTEGER,
    override_direction VARCHAR(255),
    final_strike DOUBLE PRECISION,
    final_sl DOUBLE PRECISION,
    final_target DOUBLE PRECISION,
    final_lot_size INTEGER,
    final_direction VARCHAR(255),
    UNIQUE (trade_date, signal_id)
);

CREATE INDEX idx_trade_decisions_trade_date ON trade_decisions(trade_date);
CREATE INDEX idx_trade_decisions_signal_id ON trade_decisions(signal_id);
