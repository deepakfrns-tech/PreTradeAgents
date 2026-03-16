# Changelog

All notable changes to the PreTradeAgents project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- Full analysis pipeline (`market_analyst/pipeline.py`): collect NSE pre-market data → enrich with option chains & news → score with Claude → save to DB
- `POST /api/analyst/run` endpoint to trigger the pipeline with optional `date`, `min_gap`, `top_n` params
- `anthropic` dependency in `requirements.txt` for Claude API scoring

### Fixed
- Market Analyst agent now returns useful JSON at root URL (`/`) instead of 404, listing all available endpoints

### Changed (BREAKING) — Complete Java to Python Refactor
- **Entire codebase migrated from Java/Spring Boot to Python/Flask**
- Removed all Java source code, Maven POMs, and Spring Boot configurations
- Python code promoted from `py/` subdirectory to root-level modules
- Updated all documentation to reference Python/Flask instead of Java/Spring Boot:
  README.md, architecture.md, runbooks, ADRs, skills, hooks

### Added
- `shared/` — SQLAlchemy models, database, time_utils, formatters, lot_sizes, nse_client
- `shared/nse_client.py` — NSE API client with session warmup, retry logic, exponential backoff
- `market_analyst/` — Flask REST API (port 8081) with CSV export
- `market_analyst/collectors/nse_collector.py` — Pre-market data, option chains, market snapshots
- `market_analyst/collectors/news_collector.py` — Google News RSS + MoneyControl scraping
- `market_analyst/collectors/technical_collector.py` — Volume/VWAP calculations (stub for broker API)
- `trade_dashboard/` — Flask web app (port 8080) with Jinja2 templates, CSV upload, trade approval
- `trade_executor/` — Flask app (port 8082) with APScheduler 9:15 AM cron + position monitoring
- `learning_summary/` — Flask app (port 8083) with daily summary aggregation + pattern mining
- `tests/` — pytest test suite (40 tests) for time_utils, formatters, lot_sizes, technical_collector, csv_parser
- `requirements.txt` — Python dependencies (Flask, SQLAlchemy, psycopg2, APScheduler, requests, pytest)
- `Dockerfile.analyst`, `Dockerfile.dashboard`, `Dockerfile.executor`, `Dockerfile.learner` — Per-service Python Dockerfiles
- `scripts/run.sh` — Run individual agents or full stack
- `scripts/build.sh` — Install deps and verify imports, or build Docker images
- `scripts/test.sh` — Run pytest test suite

### Removed
- All Java source code (`agent-market-analyst/src/`, `agent-trade-executor/src/`, etc.)
- All Maven POM files
- All Spring Boot configurations and Dockerfiles
- `shared-db/src/` and `shared-utils/src/` (Java models and utilities)
- `py/` subdirectory (code promoted to root level)

## [1.0.0] - 2026-03-15

### Added
- Initial multi-agent architecture with three Spring Boot microservices
- **Agent 1 - Market Analyst** (port 8081)
  - NseCollector: Pre-market data and option chain collection from NSE API
  - NewsCollector: Google News RSS and MoneyControl sentiment scraping
  - TechnicalCollector: Volume and VWAP analysis (stub for broker API)
  - AnalystSettings: Configurable gap threshold, scoring weights, top-N stocks
- **Agent 2 - Trade Executor** (port 8082)
  - Paper trade execution framework
  - Position monitoring with trailing stop loss
  - PnL tracking and trade lifecycle management
- **Agent 3 - Learning Summary** (port 8083)
  - Daily performance aggregation
  - Pattern mining via Claude Opus
  - Strategy recommendation generation
- **shared-db**: 6 JPA entity models with Flyway migrations
  - MarketSnapshot, StockAnalysis, TradeDecision
  - PaperTrade, DailySummary, StrategyLearning
- **shared-utils**: Cross-agent utility classes
  - NseClient: HTTP client with retry logic and session warmup
  - TimeUtils: IST timezone and market hours utilities
  - Formatters: INR, percentage, and PnL formatting
  - LotSizes: F&O lot size registry for 50+ NSE symbols
- Claude AI integration (Sonnet for agents 1&2, Opus for agent 3)
- PostgreSQL database with JSONB support for flexible data storage
