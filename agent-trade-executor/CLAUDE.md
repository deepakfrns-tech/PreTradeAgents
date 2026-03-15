# agent-trade-executor — Local Context

## What This Agent Does
Agent 2. Runs during market hours (9:15 AM - 3:30 PM IST). Reads approved trade decisions, validates entry conditions at market open, executes paper trades, monitors positions with trailing stop loss, and records PnL outcomes.

## Data Flow

```
TradeDecision (from Dashboard) → @Scheduled 9:15 AM → PaperTrade (execution + monitoring)
```

## Key Config (ExecutorSettings)

- `maxPositions` — maximum simultaneous paper trades
- `maxLossPercent` — daily loss limit trigger
- `trailingStopPercent` — trailing stop loss percentage
- `eodExitTime` — forced exit time for open positions
- `monitoringInterval` — position check frequency

## Key Files

| File | Purpose |
|------|---------|
| `service/TradeExecutionService.java` | 9:15 cron trigger, position monitoring, trade closure |
| `controller/ExecutorController.java` | REST API (manual trigger, trade queries) |
| `db/TradeDecisionRepository.java` | Reads approved decisions |
| `db/PaperTradeRepository.java` | Paper trade CRUD |

## Danger Zones

- **PaperTrade JSONB fields** (`priceTrail`, `slAdjustments`) — must be valid JSON strings
- **Trade lifecycle** — entry → monitoring → exit must update status correctly
- **Time-sensitive** — uses `@EnableScheduling` for 9:15 AM trigger and position monitoring
- **EOD exit** — positions are force-closed at `eodExitTime` (default 15:15)

## Build & Test

```bash
# Requires shared-db and shared-utils installed first
mvn clean package
mvn test
```
