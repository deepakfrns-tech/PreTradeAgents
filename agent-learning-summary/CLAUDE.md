# agent-learning-summary — Local Context

## What This Agent Does
Agent 3. Runs post-market (~4:00 PM IST). Aggregates the day's trades into a DailySummary, mines patterns using Claude Opus (8k tokens), and produces StrategyLearning records with confidence scores.

## Data Flow

```
PaperTrade + StockAnalysis + TradeDecision (from DB)
    → LearningSummaryService.generateDailySummary() → DailySummary (DB)
    → LearningSummaryService.minePatterns() → StrategyLearning (DB)
```

## Key Files

| File | Purpose |
|------|---------|
| `service/LearningSummaryService.java` | Daily summary generation + pattern mining |
| `controller/LearnerController.java` | REST API for summaries and learnings |
| `db/PaperTradeRepository.java` | Trade queries |
| `db/DailySummaryRepository.java` | Summary persistence |
| `db/StrategyLearningRepository.java` | Learning persistence |
| `db/StockAnalysisRepository.java` | Signal queries for cross-referencing |
| `db/TradeDecisionRepository.java` | Decision queries for accuracy |

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
