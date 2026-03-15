# agent-trade-executor — Local Context

## What This Agent Does
Agent 2. Runs during market hours (9:15 AM - 3:30 PM IST). Reads approved trade decisions, validates entry conditions at market open, executes paper trades, monitors positions with trailing stop loss, and records PnL outcomes.

## Data Flow

```
StockAnalysis (from DB) → TradeDecision (approval) → PaperTrade (execution + monitoring)
```

## Key Config (ExecutorSettings)

- `maxPositions` — maximum simultaneous paper trades
- `maxLossPercent` — daily loss limit trigger
- `trailingStopPercent` — trailing stop loss percentage
- `eodExitTime` — forced exit time for open positions
- `monitoringInterval` — position check frequency

## Danger Zones

- **PaperTrade JSONB fields** (`priceTrail`, `slAdjustments`) — must be valid JSON strings
- **Trade lifecycle** — entry → monitoring → exit must update status correctly
- **Time-sensitive** — uses `@EnableScheduling` for position monitoring

## Build & Test

```bash
# Requires shared-db and shared-utils installed first
mvn clean package
mvn test
```
