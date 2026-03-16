# Architecture - PreTradeAgents

## System Overview

PreTradeAgents is a multi-agent trading analysis system built on a microservices architecture. Three independent Python/Flask agents plus a web dashboard collaborate through a shared PostgreSQL database to perform pre-market analysis, trade approval, paper trade execution, and strategy learning. Each component can run independently.

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
│   shared/            │     │   shared-db/      │
│  (SQLAlchemy models, │     │  (SQL migrations)  │
│   time_utils,        │     │                    │
│   formatters,        │     │                    │
│   lot_sizes,         │     │                    │
│   nse_client)        │     │                    │
└──────────┬──────────┘     └────────────────────┘
           │
           │    ┌─────────────────────┐
           │    │                     │
     ┌─────▼────▼───┐  ┌──────────────┐  ┌────────────────┐  ┌──────────────────┐
     │market_analyst │  │trade_        │  │trade_executor  │  │learning_summary  │
     │(8081)         │  │dashboard     │  │(8082)          │  │(8083)            │
     │               │  │(8080)        │  │                │  │                  │
     └───────────────┘  └──────────────┘  └────────────────┘  └──────────────────┘
```

### Package Structure

```
PreTradeAgents/
├── shared/                    # Shared library
│   ├── models.py              # SQLAlchemy models (6 tables)
│   ├── database.py            # DB session management
│   ├── time_utils.py          # IST timezone utilities
│   ├── formatters.py          # INR/percentage formatters
│   ├── lot_sizes.py           # F&O lot size registry
│   └── nse_client.py          # NSE API HTTP client
├── market_analyst/            # Agent 1
│   ├── app.py                 # Flask app (port 8081)
│   ├── csv_export.py          # CSV export service
│   └── collectors/
│       ├── nse_collector.py   # Pre-market + option chain data
│       ├── news_collector.py  # Google News + MoneyControl
│       └── technical_collector.py  # Volume/VWAP calculations
├── trade_dashboard/           # Web Dashboard
│   ├── app.py                 # Flask app (port 8080)
│   ├── csv_parser.py          # CSV parsing service
│   ├── templates/             # Jinja2 templates
│   └── static/css/            # Stylesheets
├── trade_executor/            # Agent 2
│   └── app.py                 # Flask app (port 8082) + APScheduler
└── learning_summary/          # Agent 3
    └── app.py                 # Flask app (port 8083)
```

## Data Flow

### 1. Pre-Market Phase (9:00-9:15 AM IST)

```
NSE Pre-Market API ──> nse_collector.collect_pre_market_data()
                           │
                           ▼
                    List[PreMarketEntry]
                    (symbol, gap%, IEP, volume)
                           │
Google News RSS ────> news_collector.collect_news(symbol)
                           │
                           ▼
                    List[NewsItem]
                    (headline, source, date)
                           │
NSE Option Chain ──> nse_collector.collect_option_chain(symbol)
                           │
                           ▼
                    OptionChainData
                    (OI, IV, volumes per strike)
                           │
                    All data combined ──> Claude AI Analysis
                           │
                           ▼
                    StockAnalysis (saved to DB)
                    - composite_score (0-100)
                    - signal_direction (BULLISH/BEARISH)
                    - entry parameters (strike, SL, target)
                    - claude_reasoning (AI explanation)
                           │
                    csv_export ──> trade-signals-YYYY-MM-DD.csv
```

### 1.5. Dashboard Phase (before 9:15 AM IST)

```
CSV File (from Market Analyst) ──> Upload to Dashboard (port 8080)
                                         │
                                    csv_parser ──> StockAnalysis (saved to DB)
                                         │
                                    Dashboard View (scores, direction, reasoning)
                                         │
                                    User selects trades ──> TradeDecision (DB, decision=APPROVED)
```

### 2. Market Hours Phase (9:15 AM - 3:30 PM IST)

```
TradeDecision (APPROVED, from DB) ──> APScheduler 9:15 AM IST trigger
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
- Gap analysis: `gap_percent`, `gap_direction`, `gap_category`, `gap_score`
- Sentiment: `sentiment_score`, `sentiment_level`, `sentiment_reasoning`
- Volume: `volume_ratio`, `volume_level`, `vwap_position`, `volume_score`
- Options: `pcr`, `oi_buildup`, `max_pain`, `iv_percentile`, `oi_score`
- AI output: `composite_score`, `signal_direction`, `claude_reasoning`
- Entry params: `entry_strike`, `estimated_premium`, `stop_loss`, `target`, `risk_reward_ratio`

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

```python
# Via environment variable
ANTHROPIC_API_KEY = os.environ.get('ANTHROPIC_API_KEY')
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

All agents read from environment variables:

### Common (all agents)

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=pretrade
DB_USERNAME=pretrade
DB_PASSWORD=pretrade
ANTHROPIC_API_KEY=sk-ant-...
```

### Agent-Specific

- **analyst**: gap_threshold (0.5%), min_composite_score (60.0), top_n_stocks (20)
- **executor**: max_positions (5), max_loss_per_trade (₹2,000), trailing_stop_percent (30%)
- **learner**: min_confidence (0.7), lookback_days (30), min_occurrences (3)

## Security Considerations

- API keys stored as environment variables, never in code or config files
- NSE API calls use browser-like headers to avoid blocking
- Database credentials via environment variables
- No authentication between agents (internal network assumed)
