# agent-learning-summary — Local Context

## What This Agent Does
Agent 3. Runs post-market (~4:00 PM IST). Aggregates the day's trades into a DailySummary, mines patterns using Claude Opus (8k tokens), and produces StrategyLearning records with confidence scores.

## Data Flow

```
PaperTrade (all day's trades) → DailySummary (aggregation) → StrategyLearning (pattern mining)
```

## Key Config (LearnerSettings)

- `minConfidence` — minimum confidence score to accept a learning
- `lookbackDays` — number of days to analyze for patterns
- `patternOccurrenceThreshold` — minimum occurrences before a pattern is considered valid

## Claude Usage

Uses **Claude Opus** (not Sonnet) with **8192 max tokens** for deeper analysis. This is intentional — strategy extraction requires more sophisticated reasoning than real-time analysis.

## Danger Zones

- **DailySummary JSONB fields** (`tradePostmortems`, `overrideImpact`, `patterns`, `adjustments`) — complex nested JSON
- **StrategyLearning confidence** — validated against `minConfidence` threshold
- **Lookback queries** — can be slow if lookbackDays is large; consider indexing

## Build & Test

```bash
# Requires shared-db and shared-utils installed first
mvn clean package
mvn test
```
