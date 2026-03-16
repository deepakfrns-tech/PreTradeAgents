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
              ┌─────────────────┐
              │  Trade Dashboard │
              │  (Port 8080)     │
              │  CSV upload +    │
              │  trade approval  │
              └────────┬─────────┘
                       │
           ┌───────────┴───────────┐
           │   PostgreSQL (shared)  │
           │   shared/ models       │
           └───────────────────────┘
```

## Quick Start (Docker)

The fastest way to run everything locally:

```bash
# 1. Clone and set up environment
cp .env.example .env
# Edit .env — add your ANTHROPIC_API_KEY

# 2. Start all services (PostgreSQL + 4 apps)
./scripts/local-deploy.sh up

# 3. Check status
./scripts/local-deploy.sh status

# 4. View logs
./scripts/local-deploy.sh logs

# 5. Stop
./scripts/local-deploy.sh down
```

This starts PostgreSQL, applies SQL migrations, and boots all agents automatically.

## Manual Setup (No Docker)

### Prerequisites

- Python 3.11+
- PostgreSQL 15+
- Anthropic API key

### Steps

```bash
# 1. Database
createdb pretrade

# 2. Environment variables
export DB_HOST=localhost DB_PORT=5432 DB_NAME=pretrade
export DB_USERNAME=pretrade DB_PASSWORD=pretrade
export ANTHROPIC_API_KEY=sk-ant-your-key-here

# 3. Install dependencies
pip install -r requirements.txt

# 4. Apply migrations
./scripts/run.sh init-db

# 5. Run each agent (separate terminals)
python -m market_analyst.app      # Port 8081
python -m trade_dashboard.app     # Port 8080
python -m trade_executor.app      # Port 8082
python -m learning_summary.app    # Port 8083
```

## Project Structure

```
PreTradeAgents/
├── shared/                  # SQLAlchemy models, DB, time_utils, formatters, lot_sizes, nse_client
├── market_analyst/          # Agent 1 - Pre-market data & AI analysis (port 8081)
│   └── collectors/          # NseCollector, NewsCollector, TechnicalCollector
├── trade_dashboard/         # Web Dashboard - CSV upload, signal review, trade approval (port 8080)
│   ├── templates/           # Jinja2 templates (base, dashboard, upload)
│   └── static/css/          # Dark-themed responsive styling
├── trade_executor/          # Agent 2 - Paper trade execution (port 8082)
├── learning_summary/        # Agent 3 - Daily learning & strategy (port 8083)
├── shared-db/migrations/    # SQL migrations (V001-V007)
├── tests/                   # pytest test suite (40 tests)
├── docs/                    # Architecture, ADRs, runbooks
├── scripts/                 # Build, test, deploy scripts
├── .claude/                 # AI assistant config (skills, hooks)
├── docker-compose.yml       # Local deployment
├── requirements.txt         # Python dependencies
├── CLAUDE.md                # AI assistant guide
├── CHANGELOG.md             # Version history
└── COMMIT_LOG.md            # Commit logs & functional changes
```

## Scripts

| Script | Purpose |
|--------|---------|
| `./scripts/build.sh` | Install dependencies and verify imports |
| `./scripts/build.sh docker` | Build Docker images |
| `./scripts/test.sh` | Run all pytest tests |
| `./scripts/test.sh tests/test_formatters.py` | Run a specific test file |
| `./scripts/run.sh postgres` | Start PostgreSQL + apply migrations |
| `./scripts/run.sh analyst` | Run Market Analyst (port 8081) |
| `./scripts/run.sh dashboard` | Run Trade Dashboard (port 8080) |
| `./scripts/run.sh executor` | Run Trade Executor (port 8082) |
| `./scripts/run.sh learner` | Run Learning Summary (port 8083) |
| `./scripts/local-deploy.sh up` | Docker: start everything |
| `./scripts/local-deploy.sh down` | Docker: stop everything |
| `./scripts/local-deploy.sh logs` | Docker: tail all logs |
| `./scripts/local-deploy.sh status` | Docker: show running services |

## Agent Workflow

| Time (IST) | Agent | Action |
|-------------|-------|--------|
| 9:00-9:15 | Market Analyst | Collect pre-market gaps, news, options data; score with Claude |
| 9:15-15:30 | Trade Executor | Validate signals, execute paper trades, monitor positions |
| ~16:00 | Learning Summary | Aggregate results, extract patterns, produce recommendations |

## Running Tests

```bash
# All tests
./scripts/test.sh

# Specific test file
python -m pytest tests/test_formatters.py -v
```

## Documentation

| Document | Purpose |
|----------|---------|
| [CLAUDE.md](CLAUDE.md) | AI assistant guide (purpose, rules, commands) |
| [Architecture](docs/architecture.md) | System design and data flow |
| [ADRs](docs/decisions/) | Architecture Decision Records |
| [Runbooks](docs/runbooks/) | Operational guides (build, deploy, DB ops) |
| [CHANGELOG](CHANGELOG.md) | Version history |
| [COMMIT_LOG](COMMIT_LOG.md) | Detailed commit and change tracking |

## License

Private - All rights reserved.
