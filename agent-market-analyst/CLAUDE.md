# agent-market-analyst — Local Context

## What This Agent Does
Agent 1. Runs during pre-market (9:00-9:15 AM IST). Collects NSE pre-market data, scrapes news sentiment, analyzes options flow, and produces Claude-scored trading signals saved to `stock_analysis` table.

## Architecture

```
NseCollector ──┐
NewsCollector ──┼──> Claude AI Analysis ──> StockAnalysis (DB)
TechnicalCollector─┘
```

## Key Files

| File | Purpose | Danger Level |
|------|---------|-------------|
| `collectors/NseCollector.java` | NSE API calls + parsing. DTOs: PreMarketEntry, OptionChainData, OptionEntry, MarketSnapshotData | HIGH — NSE blocks on rate limits |
| `collectors/NewsCollector.java` | Google News RSS + MoneyControl scraping. DTO: NewsItem | MEDIUM — external site changes break parsing |
| `collectors/TechnicalCollector.java` | Volume/VWAP calculations (stub for broker API) | LOW — mostly static calculations |
| `config/AnalystSettings.java` | Scoring weights, thresholds, timeout config | LOW |

## Scoring Weights (default)

| Factor | Weight | Source |
|--------|--------|--------|
| Gap | 25% | NseCollector |
| Sentiment | 20% | NewsCollector |
| Volume | 20% | TechnicalCollector |
| OI | 20% | NseCollector (option chain) |
| Alignment | 15% | Cross-factor |

## Adding a New Collector

1. Create `@Service` class in `collectors/`
2. Inject `AnalystSettings` for timeout config
3. Define DTOs as static inner classes (`@Data @Builder @NoArgsConstructor @AllArgsConstructor`)
4. Add tests in `src/test/java/com/pretrade/analyst/collectors/`

## Build & Test

```bash
# Requires shared-db and shared-utils installed first
mvn clean package
mvn test
```
