CREATE TABLE strategy_learnings (
    id SERIAL PRIMARY KEY,
    learning_date DATE,
    category VARCHAR(255),
    insight TEXT,
    evidence JSONB,
    confidence DOUBLE PRECISION,
    times_validated INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP
);

CREATE INDEX idx_strategy_learnings_learning_date ON strategy_learnings(learning_date);
CREATE INDEX idx_strategy_learnings_category ON strategy_learnings(category);
CREATE INDEX idx_strategy_learnings_is_active ON strategy_learnings(is_active);
