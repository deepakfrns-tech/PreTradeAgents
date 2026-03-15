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

## Quick Start (Docker)

The fastest way to run everything locally:

```bash
# 1. Clone and set up environment
cp .env.example .env
# Edit .env — add your ANTHROPIC_API_KEY

# 2. Start all services (PostgreSQL + 3 agents)
./scripts/local-deploy.sh up

# 3. Check status
./scripts/local-deploy.sh status

# 4. View logs
./scripts/local-deploy.sh logs

# 5. Stop
./scripts/local-deploy.sh down
```

This starts PostgreSQL, runs Flyway migrations, and boots all three agents automatically.

## Manual Setup (No Docker)

### Prerequisites

- Java 21 JDK
- Maven 3.8+
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

# 3. Build all modules
./scripts/build.sh

# 4. Run each agent (separate terminals)
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
├── docs/                     # Architecture, ADRs, runbooks
├── scripts/                  # Build, test, deploy scripts
├── .claude/                  # AI assistant config (skills, hooks)
├── docker-compose.yml        # Local deployment
├── CLAUDE.md                 # AI assistant guide
├── CHANGELOG.md              # Version history
└── COMMIT_LOG.md             # Commit logs & functional changes
```

## Scripts

| Script | Purpose |
|--------|---------|
| `./scripts/build.sh` | Build all modules in correct order |
| `./scripts/build.sh --skip-tests` | Build without running tests |
| `./scripts/test.sh` | Run tests across all modules |
| `./scripts/test.sh shared-utils` | Test a specific module |
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
# All modules
./scripts/test.sh

# Single module
./scripts/test.sh shared-utils
cd shared-utils && mvn test
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
