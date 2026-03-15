# shared-db — Local Context

## What This Module Does
Shared JPA entity models and Flyway database migrations. All 6 database tables are defined here. Every agent depends on this module.

## Danger Zones

- **Never modify existing Flyway migrations** (V001-V006). They've been applied. Create new ones instead.
- **Entity field names map to DB columns** — renaming a field breaks the schema unless you also create a migration.
- **JSONB columns** are stored as `String` in Java with `@Column(columnDefinition = "jsonb")`. Parse with Jackson when reading.

## Key Files

| File | Purpose |
|------|---------|
| `migrations/V001-V006` | Flyway SQL migrations (applied in order) |
| `StockAnalysis.java` | Core signal model — most fields, unique on (tradeDate, symbol) |
| `MarketSnapshot.java` | Daily market conditions |
| `TradeDecision.java` | FK to StockAnalysis via signalId |
| `PaperTrade.java` | Trade execution records with JSONB price trail |
| `DailySummary.java` | Aggregated daily metrics |
| `StrategyLearning.java` | Mined patterns with confidence scores |

## Build

```bash
mvn clean install   # Installs to local Maven repo for agents to consume
mvn test            # Run model unit tests
```
