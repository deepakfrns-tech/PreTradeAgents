# PreTradeAgents

A multi-agent, Claude-powered pre-market analysis and paper trading system for the National Stock Exchange of India (NSE).

## What It Does

PreTradeAgents runs three specialized AI agents that work together to:

1. **Analyze** pre-market data (gaps, volume, sentiment, options flow) using Claude AI
2. **Execute** paper trades based on AI-scored signals with real-time monitoring
3. **Learn** from daily outcomes to refine strategy recommendations over time

## Architecture

```
┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
│  Agent 1:        │   │  Agent 2:        │   │  Agent 3:        │
│  Market Analyst  │   │  Trade Executor  │   │  Learning Summary│
│  (Port 8081)     │   │  (Port 8082)     │   │  (Port 8083)     │
│                  │   │                  │   │                  │
│  - NSE data      │   │  - Signal valid. │   │  - Daily summary │
│  - News scraping │   │  - Paper trades  │   │  - Pattern mining│
│  - Claude scoring│   │  - PnL tracking  │   │  - Strategy recs │
└────────┬─────────┘   └────────┬─────────┘   └────────┬─────────┘
         │                      │                      │
         └──────────────────────┼──────────────────────┘
                                │
                    ┌───────────┴───────────┐
                    │   PostgreSQL (shared)  │
                    │   shared-db models     │
                    │   shared-utils         │
                    └───────────────────────┘
```

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 21 (agents), Java 17 (shared libs) |
| Framework | Spring Boot 3.2.3 |
| Database | PostgreSQL with Flyway migrations |
| AI | Anthropic Claude API (Sonnet + Opus) |
| Build | Maven |
| ORM | JPA/Hibernate + Lombok |

## Prerequisites

- Java 21 JDK
- Maven 3.8+
- PostgreSQL 15+
- Anthropic API key

## Setup

### 1. Database

```bash
createdb pretrade
# Flyway migrations run automatically on agent startup
```

### 2. Environment Variables

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=pretrade
export DB_USERNAME=pretrade
export DB_PASSWORD=pretrade
export ANTHROPIC_API_KEY=sk-ant-your-key-here
```

### 3. Build

```bash
# Build shared libraries first
cd shared-db && mvn clean install && cd ..
cd shared-utils && mvn clean install && cd ..

# Build agents
cd agent-market-analyst && mvn clean package && cd ..
cd agent-trade-executor && mvn clean package && cd ..
cd agent-learning-summary && mvn clean package && cd ..
```

### 4. Run

Start each agent in a separate terminal:

```bash
java -jar agent-market-analyst/target/agent-market-analyst-1.0.0-SNAPSHOT.jar
java -jar agent-trade-executor/target/agent-trade-executor-1.0.0-SNAPSHOT.jar
java -jar agent-learning-summary/target/agent-learning-summary-1.0.0-SNAPSHOT.jar
```

## Project Structure

```
PreTradeAgents/
├── agent-market-analyst/     # Agent 1 - Pre-market data & AI analysis
├── agent-trade-executor/     # Agent 2 - Paper trade execution
├── agent-learning-summary/   # Agent 3 - Daily learning & strategy
├── shared-db/                # JPA entities & Flyway migrations
├── shared-utils/             # Common utilities (NSE client, formatters)
├── docs/                     # Architecture documentation
├── CLAUDE.md                 # AI assistant guide
├── CHANGELOG.md              # Version history
└── COMMIT_LOG.md             # Commit logs & functional changes
```

## Agent Workflow

| Time (IST) | Agent | Action |
|-------------|-------|--------|
| 9:00-9:15 | Market Analyst | Collect pre-market gaps, news, options data; score with Claude |
| 9:15-15:30 | Trade Executor | Validate signals, execute paper trades, monitor positions |
| ~16:00 | Learning Summary | Aggregate results, extract patterns, produce recommendations |

## Database Tables

| Table | Purpose |
|-------|---------|
| `market_snapshots` | Broad market conditions at pre-open |
| `stock_analysis` | AI-scored trading signals |
| `trade_decisions` | Signal approval/rejection with overrides |
| `paper_trades` | Simulated trade execution & PnL |
| `daily_summaries` | End-of-day performance metrics |
| `strategy_learnings` | Discovered patterns & insights |

## Running Tests

```bash
# Test a specific module
cd shared-utils && mvn test

# Test all modules
for dir in shared-db shared-utils agent-market-analyst agent-trade-executor agent-learning-summary; do
  cd $dir && mvn test && cd ..
done
```

## Key Configuration

Agent-specific settings are in each module's `src/main/resources/application.yml`:

- **Market Analyst**: Gap threshold (0.5%), composite score minimum (60.0), scoring weights
- **Trade Executor**: Max positions, max loss limits, trailing stop %, EOD exit time
- **Learning Summary**: Min confidence, lookback days, pattern occurrence threshold

## Documentation

- [Architecture](docs/architecture.md) - System design and data flow
- [CLAUDE.md](CLAUDE.md) - Guide for AI assistants working on this codebase
- [CHANGELOG](CHANGELOG.md) - Version history
- [COMMIT_LOG](COMMIT_LOG.md) - Detailed commit and change tracking

## License

Private - All rights reserved.
