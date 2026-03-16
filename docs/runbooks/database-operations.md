# Runbook: Database Operations

## Adding a New Table

1. Create the SQLAlchemy model in `shared/models.py`
2. Create SQL migration: `shared-db/migrations/V00N__description.sql`
3. Use the next sequential version number (check existing: V001-V007)
4. Apply migration: `./scripts/run.sh init-db`
5. Add tests in `tests/`

## Adding a Column to Existing Table

1. Create a new SQL migration (NEVER modify existing ones):
   ```sql
   ALTER TABLE table_name ADD COLUMN column_name data_type;
   ```
2. Update the corresponding SQLAlchemy model in `shared/models.py`
3. Apply migration: `./scripts/run.sh init-db`

## Current Schema (V001-V007)

| Migration | Table | Key Columns |
|-----------|-------|-------------|
| V001 | `market_snapshots` | trade_date, nifty_gap_percent, india_vix |
| V002 | `stock_analysis` | trade_date, symbol, composite_score, signal_direction |
| V003 | `trade_decisions` | signal_id (FK), decision, overrides |
| V004 | `paper_trades` | entry_price, exit_price, pnl_amount, status |
| V005 | `daily_summaries` | total_trades, win_rate, profit_factor, sharpe_ratio |
| V006 | `strategy_learnings` | category, insight, confidence, is_active |
| V007 | `fix_serial_to_bigint` | Upgrades all SERIAL PKs to BIGINT |

## JSONB Columns

These columns store flexible JSON data:
- `market_snapshots.fii_dii_data`, `raw_pre_market_data`
- `stock_analysis.headline_details`
- `paper_trades.price_trail`, `sl_adjustments`
- `daily_summaries.trade_postmortems`, `override_impact`, `patterns_identified`
- `strategy_learnings.evidence`

Query JSONB: `SELECT * FROM stock_analysis WHERE headline_details::jsonb->>'key' = 'value';`
