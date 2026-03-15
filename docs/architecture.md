# Architecture - PreTradeAgents

## System Overview

PreTradeAgents is a multi-agent trading analysis system built on a microservices architecture. Three independent Spring Boot agents plus a web dashboard collaborate through a shared PostgreSQL database to perform pre-market analysis, trade approval, paper trade execution, and strategy learning. Each component can run independently.

## High-Level Architecture

```
                        ┌─────────────────────────────────┐
                        │         External Data Sources    │
                        │  ┌─────┐  ┌────────┐  ┌──────┐ │
                        │  │ NSE │  │ Google  │  │Money │ │
                        │  │ API │  │ News    │  │Ctrl  │ │
                        │  └──┬──┘  └───┬────┘  └──┬───┘ │
                        └─────┼─────────┼──────────┼─────┘
                              │         │          │
                    ┌─────────▼─────────▼──────────▼───────────┐
                    │        Agent 1: Market Analyst            │
                    │        (Port 8081, 9:00-9:15 IST)        │
                    │                                          │
                    │  ┌────────────┐ ┌────────────┐ ┌──────┐ │
                    │  │NseCollector│ │NewsCollector│ │Tech  │ │
                    │  │            │ │            │ │Coll. │ │
                    │  └─────┬──────┘ └─────┬──────┘ └──┬───┘ │
                    │        └──────────────┼───────────┘     │
                    │                       ▼                  │
                    │              ┌──────────────┐            │
                    │              │ Claude AI    │            │
                    │              │ (Sonnet 4k)  │            │
                    │              └──────┬───────┘            │
                    │                     ▼                     │
                    │           Scored StockAnalysis            │
                    │              + CSV Export                 │
                    └─────────────────┬────────────────────────┘
                                      │ writes to DB + CSV
                    ┌─────────────────▼────────────────────────┐
                    │      Trade Dashboard (Port 8080)          │
                    │  Upload CSV → View Signals → Approve      │
                    └─────────────────┬────────────────────────┘
                                      │ writes TradeDecisions
                    ┌─────────────────▼────────────────────────┐
                    │              PostgreSQL                   │
                    │                                          │
                    │  ┌─────────────────┐ ┌────────────────┐ │
                    │  │market_snapshots  │ │stock_analysis  │ │
                    │  └─────────────────┘ └────────────────┘ │
                    │  ┌─────────────────┐ ┌────────────────┐ │
                    │  │trade_decisions   │ │paper_trades    │ │
                    │  └─────────────────┘ └────────────────┘ │
                    │  ┌─────────────────┐ ┌────────────────┐ │
                    │  │daily_summaries   │ │strategy_learns │ │
                    │  └─────────────────┘ └────────────────┘ │
                    └──────┬──────────────────────┬────────────┘
                           │ reads                │ reads
                    ┌──────▼──────────┐    ┌──────▼──────────┐
                    │ Agent 2: Trade  │    │ Agent 3: Learn  │
                    │ Executor        │    │ Summary         │
                    │ (8082, 9:15-    │    │ (8083, ~16:00   │
                    │  15:30 IST)     │    │  IST)           │
                    │                 │    │                 │
                    │ - Validate      │    │ - Aggregate     │
                    │ - Execute paper │    │ - Mine patterns │
                    │ - Monitor PnL   │    │ - Recommend     │
                    │ - Trail SL      │    │ - Claude Opus   │
                    └─────────────────┘    └─────────────────┘
```

## Module Architecture

### Dependency Graph

```
┌─────────────────────┐     ┌──────────────────┐
│    shared-db         │     │   shared-utils    │
│  (JPA entities,     │     │  (NseClient,      │
│   Flyway migrations)│     │   TimeUtils,      │
│                     │     │   Formatters,      │
│                     │     │   LotSizes)        │
└──────────┬──────────┘     └────────┬───────────┘
           │                          │
           │    ┌─────────────────────┤
           │    │                     │
     ┌─────▼────▼───┐  ┌──────────────┐  ┌─────────────▼──┐  ┌──────────────────┐
     │agent-market-  │  │trade-        │  │agent-trade-    │  │agent-learning-   │
     │analyst        │  │dashboard     │  │executor        │  │summary           │
     │(8081)         │  │(8080)        │  │(8082)          │  │(8083)            │
     └───────────────┘  └──────────────┘  └────────────────┘  └──────────────────┘
```

### Package Structure

```
com.pretrade
├── shared.models          # JPA entities (shared-db)
│   ├── MarketSnapshot
│   ├── StockAnalysis
│   ├── TradeDecision
│   ├── PaperTrade
│   ├── DailySummary
│   └── StrategyLearning
├── utils                  # Utilities (shared-utils)
│   ├── NseClient          # NSE API HTTP client
│   ├── TimeUtils          # IST timezone utilities
│   ├── Formatters         # INR/percentage formatters
│   └── LotSizes           # F&O lot size registry
├── analyst                # Agent 1 (agent-market-analyst)
│   ├── MarketAnalystApplication
│   ├── config.AnalystSettings
│   ├── collectors
│   │   ├── NseCollector
│   │   ├── TechnicalCollector
│   │   └── NewsCollector
│   ├── service.CsvExportService
│   ├── controller.AnalystController
│   └── db.StockAnalysisRepository
├── dashboard              # Trade Dashboard (trade-dashboard)
│   ├── TradeDashboardApplication
│   ├── controller.DashboardController
│   ├── service.CsvParserService
│   └── db.{StockAnalysisRepository, TradeDecisionRepository}
├── executor               # Agent 2 (agent-trade-executor)
│   ├── TradeExecutorApplication
│   ├── config.ExecutorSettings
│   ├── service.TradeExecutionService
│   ├── controller.ExecutorController
│   └── db.{TradeDecisionRepository, PaperTradeRepository}
└── learner                # Agent 3 (agent-learning-summary)
    ├── LearningSummaryApplication
    ├── config.LearnerSettings
    ├── service.LearningSummaryService
    ├── controller.LearnerController
    └── db.{PaperTradeRepository, DailySummaryRepository, StrategyLearningRepository, ...}
```

## Data Flow

### 1. Pre-Market Phase (9:00-9:15 AM IST)

```
NSE Pre-Market API ──> NseCollector.collectPreMarketData()
                           │
                           ▼
                    List<PreMarketEntry>
                    (symbol, gap%, IEP, volume)
                           │
Google News RSS ────> NewsCollector.collectNews(symbol)
                           │
                           ▼
                    List<NewsItem>
                    (headline, source, date)
                           │
NSE Option Chain ──> NseCollector.collectOptionChain(symbol)
                           │
                           ▼
                    OptionChainData
                    (OI, IV, volumes per strike)
                           │
                    All data combined ──> Claude AI Analysis
                           │
                           ▼
                    StockAnalysis (saved to DB)
                    - compositeScore (0-100)
                    - signalDirection (BULLISH/BEARISH)
                    - entry parameters (strike, SL, target)
                    - claudeReasoning (AI explanation)
                           │
                    CsvExportService ──> trade-signals-YYYY-MM-DD.csv
```

### 1.5. Dashboard Phase (before 9:15 AM IST)

```
CSV File (from Market Analyst) ──> Upload to Dashboard (port 8080)
                                         │
                                    CsvParserService ──> StockAnalysis (saved to DB)
                                         │
                                    Dashboard View (scores, direction, reasoning)
                                         │
                                    User selects trades ──> TradeDecision (DB, decision=APPROVED)
```

### 2. Market Hours Phase (9:15 AM - 3:30 PM IST)

```
TradeDecision (APPROVED, from DB) ──> @Scheduled 9:15 AM IST trigger
                                    │
                                    ▼
                            PaperTrade (created on entry)
                                    │
                              ┌─────┴─────┐
                              │ Monitoring │ (price updates, SL trail)
                              └─────┬─────┘
                                    │
                                    ▼
                            PaperTrade (updated with exit, PnL)
```

### 3. Post-Market Phase (~4:00 PM IST)

```
All day's PaperTrades ──> DailySummary
                          (wins, losses, win rate, Sharpe)
                                │
                                ▼
                          Claude Opus Analysis (8k tokens)
                                │
                                ▼
                          StrategyLearning
                          (patterns, confidence, recommendations)
```

## Database Schema

### Entity Relationship Diagram

```
MarketSnapshot (1 per day)
    │
    │ same trade_date
    ▼
StockAnalysis (N per day, one per symbol)
    │
    │ FK: signal_id
    ▼
TradeDecision (0-1 per StockAnalysis)
    │
    │ linked by trade_date + symbol
    ▼
PaperTrade (0-1 per approved TradeDecision)
    │
    │ aggregated into
    ▼
DailySummary (1 per day)
    │
    │ learnings extracted
    ▼
StrategyLearning (N per day)
```

### Key Columns

**StockAnalysis** - Core signal with multi-factor scoring:
- Gap analysis: `gapPercent`, `gapDirection`, `gapCategory`, `gapScore`
- Sentiment: `sentimentScore`, `sentimentLevel`, `sentimentReasoning`
- Volume: `volumeRatio`, `volumeLevel`, `vwapPosition`, `volumeScore`
- Options: `pcr`, `oiBuildup`, `maxPain`, `ivPercentile`, `oiScore`
- AI output: `compositeScore`, `signalDirection`, `claudeReasoning`
- Entry params: `entryStrike`, `estimatedPremium`, `stopLoss`, `target`, `riskRewardRatio`

### Scoring Weights (configurable)

| Factor | Weight | Description |
|--------|--------|-------------|
| Gap | 25% | Price gap magnitude and direction |
| Sentiment | 20% | News sentiment from multiple sources |
| Volume | 20% | Volume ratio and VWAP position |
| OI (Open Interest) | 20% | Options flow and PCR analysis |
| Alignment | 15% | Cross-factor signal alignment |

## Claude AI Integration

### Agent 1 & 2: Claude Sonnet

- Model: `claude-sonnet-4-6`
- Max tokens: 4096
- Purpose: Real-time analysis and trade validation

### Agent 3: Claude Opus

- Model: `claude-opus-4-6`
- Max tokens: 8192
- Purpose: Deeper pattern analysis and strategy extraction

### API Configuration

```yaml
anthropic:
  api-key: ${ANTHROPIC_API_KEY}
  base-url: https://api.anthropic.com/v1
```

## NSE API Integration

### Endpoints Used

| Endpoint | Purpose |
|----------|---------|
| `/api/market-data-pre-open?key=FO` | Pre-market gap/volume data |
| `/api/option-chain-indices?symbol=X` | Index option chains |
| `/api/option-chain-equities?symbol=X` | Equity option chains |
| `/api/allIndices` | NIFTY, Bank NIFTY, VIX values |

### Rate Limiting Strategy

- Session warmup: Hit `nseindia.com` base URL before API calls
- Retry: 3 attempts with exponential backoff (1s, 2s, 4s)
- User-Agent spoofing: Chrome browser headers required

## Configuration

Each agent reads from `src/main/resources/application.yml`:

### Common (all agents)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    locations: classpath:db/migration
```

### Agent-Specific

- **analyst.gapThreshold**: Minimum gap % to trigger analysis (default: 0.5)
- **analyst.minCompositeScore**: Minimum score for signal generation (default: 60.0)
- **analyst.topNStocks**: Number of top stocks to analyze (default: 20)
- **executor.maxPositions**: Maximum simultaneous paper trades
- **executor.maxLossPercent**: Daily loss limit trigger
- **executor.trailingStopPercent**: Trailing stop loss percentage
- **learner.minConfidence**: Minimum confidence for pattern extraction
- **learner.lookbackDays**: Number of days to analyze for patterns

## Security Considerations

- API keys stored as environment variables, never in code or config files
- NSE API calls use browser-like headers to avoid blocking
- Database credentials via environment variables
- No authentication between agents (internal network assumed)
