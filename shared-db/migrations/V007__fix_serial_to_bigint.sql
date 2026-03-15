-- V007: Upgrade all SERIAL (int4) primary keys to BIGINT to match JPA Long mappings.
-- Must drop FK constraints first, alter referenced column, then re-add.

-- 1. Drop FK constraints that reference stock_analysis(id)
ALTER TABLE trade_decisions DROP CONSTRAINT IF EXISTS trade_decisions_signal_id_fkey;
ALTER TABLE paper_trades    DROP CONSTRAINT IF EXISTS paper_trades_signal_id_fkey;

-- 2. Upgrade each table's id column and its backing sequence
ALTER TABLE market_snapshots    ALTER COLUMN id TYPE BIGINT;
ALTER SEQUENCE market_snapshots_id_seq    AS BIGINT;

ALTER TABLE stock_analysis      ALTER COLUMN id TYPE BIGINT;
ALTER SEQUENCE stock_analysis_id_seq      AS BIGINT;

ALTER TABLE trade_decisions     ALTER COLUMN id TYPE BIGINT;
ALTER SEQUENCE trade_decisions_id_seq     AS BIGINT;

ALTER TABLE paper_trades        ALTER COLUMN id TYPE BIGINT;
ALTER SEQUENCE paper_trades_id_seq        AS BIGINT;

ALTER TABLE daily_summaries     ALTER COLUMN id TYPE BIGINT;
ALTER SEQUENCE daily_summaries_id_seq     AS BIGINT;

ALTER TABLE strategy_learnings  ALTER COLUMN id TYPE BIGINT;
ALTER SEQUENCE strategy_learnings_id_seq  AS BIGINT;

-- 3. Upgrade FK columns to BIGINT to match the new PK type
ALTER TABLE trade_decisions ALTER COLUMN signal_id TYPE BIGINT;
ALTER TABLE paper_trades    ALTER COLUMN signal_id TYPE BIGINT;

-- 4. Re-add FK constraints
ALTER TABLE trade_decisions
    ADD CONSTRAINT trade_decisions_signal_id_fkey
    FOREIGN KEY (signal_id) REFERENCES stock_analysis(id);

ALTER TABLE paper_trades
    ADD CONSTRAINT paper_trades_signal_id_fkey
    FOREIGN KEY (signal_id) REFERENCES stock_analysis(id);
