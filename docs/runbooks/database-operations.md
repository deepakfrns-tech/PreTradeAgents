# Runbook: Database Operations

## Adding a New Table

1. Create the JPA entity in `shared-db/src/main/java/com/pretrade/shared/models/`
2. Create Flyway migration: `shared-db/migrations/V00N__description.sql`
3. Use the next sequential version number (check existing: V001-V006)
4. Rebuild: `cd shared-db && mvn clean install`
5. Add tests in `shared-db/src/test/java/com/pretrade/shared/models/`

## Adding a Column to Existing Table

1. Create a new Flyway migration (NEVER modify existing ones):
   ```sql
   ALTER TABLE table_name ADD COLUMN column_name data_type;
   ```
2. Update the corresponding JPA entity class
3. Rebuild shared-db

## Current Schema (V001-V006)

| Migration | Table | Key Columns |
|-----------|-------|-------------|
| V001 | `market_snapshots` | tradeDate, niftyGapPercent, indiaVix |
| V002 | `stock_analysis` | tradeDate, symbol, compositeScore, signalDirection |
| V003 | `trade_decisions` | signalId (FK), approved, overrides |
| V004 | `paper_trades` | entryPrice, exitPrice, pnlAmount, status |
| V005 | `daily_summaries` | totalTrades, winRate, profitFactor, sharpeRatio |
| V006 | `strategy_learnings` | category, insight, confidence, isActive |

## JSONB Columns

These columns store flexible JSON data:
- `market_snapshots.fiiDiiData`, `rawPreMarketData`
- `stock_analysis.headlineDetails`
- `paper_trades.priceTrail`, `slAdjustments`
- `daily_summaries.tradePostmortems`, `overrideImpact`, `patterns`
- `strategy_learnings.evidence`

Query JSONB: `SELECT * FROM stock_analysis WHERE headlineDetails::jsonb->>'key' = 'value';`
