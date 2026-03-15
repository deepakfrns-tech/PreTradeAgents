# Changelog

All notable changes to the PreTradeAgents project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- **Trade Dashboard** web app (`trade-dashboard/`, port 8080) — upload CSV, view signals dashboard, select trades for execution
  - CSV upload with drag-and-drop support and parsed signal import to DB
  - Interactive dark-themed dashboard with signal scoring, direction badges, confidence indicators
  - Trade approval workflow: select signals → persist as TradeDecision in DB
  - Signal detail cards with Claude reasoning, risk warnings, options flow, gap analysis
- **CSV Export** for Market Analyst agent — exports StockAnalysis signals to `trade-signals-YYYY-MM-DD.csv`
  - REST endpoint `POST /api/analyst/export-csv` to generate and download CSV
  - Configurable output directory via `CSV_OUTPUT_DIR` env var
- **Trade Execution Service** with 9:15 AM IST scheduled trigger (`@Scheduled(cron)`)
  - Reads approved TradeDecisions from DB at market open
  - Creates PaperTrade entries with position limits and risk controls
  - Position monitoring with trailing stop loss and EOD forced exit
  - Manual trigger endpoint: `POST /api/executor/trigger`
- **Learning Summary Service** with daily aggregation and pattern mining
  - Generates DailySummary from trade results (win rate, PnL, profit factor)
  - Mines direction bias and timing patterns from lookback period
  - Produces StrategyLearning records with confidence scoring
  - REST endpoints for summary generation and pattern mining
- JPA repositories for all 3 agents and the dashboard
- REST controllers with health endpoints for all agents
- `scripts/run.sh` — run individual agents or full stack locally
- Docker Compose profiles for independent service execution (`analyst`, `dashboard`, `executor`, `learner`)

### Changed
- `scripts/build.sh` updated for 6 modules (added trade-dashboard), supports target module builds
- `scripts/test.sh` updated to include trade-dashboard module
- `docker-compose.yml` restructured with profiles and shared CSV volume
- Agent applications updated to scan `com.pretrade.utils` package for shared beans

### Previously Added
- Docker Compose local deployment (`docker-compose.yml`) — one command to run everything
- Dockerfiles for all 3 agents (multi-stage builds with JRE-alpine runtime)
- `.dockerignore` to optimize Docker build context
- `.env.example` template for environment variables
- Build scripts: `scripts/build.sh`, `scripts/test.sh`, `scripts/local-deploy.sh`
- `.claude/skills/doc-update/SKILL.md` — mandatory doc-update workflow skill
- Pre-commit hook in `.claude/settings.json` — reminds to update docs before committing
- `docs/runbooks/local-deployment.md` — Docker and manual deployment guide
- Mandatory doc-update rules added to `CLAUDE.md` (MUST update CHANGELOG, COMMIT_LOG, module CLAUDE.md on every change)

### Changed
- CLAUDE.md updated with Docker commands and mandatory doc-update section
- `.claude/settings.json` updated with Docker/script permissions and pre-commit hook
- `.claude/hooks/README.md` rewritten with active hook documentation
- README.md updated with Quick Start (Docker), scripts table, and deployment docs
- `.gitignore` updated with Docker entries

### Previously Added
- CLAUDE.md rewritten to be concise (purpose, repo map, rules only) per best practices
- `.claude/settings.json` with permissions (allowed/denied commands)
- `.claude/skills/` — 4 reusable AI workflow skills (code-review, refactor, debug, release)
- `.claude/hooks/README.md` — guardrail documentation for automated checks
- `docs/decisions/` — 3 Architecture Decision Records (ADRs)
- `docs/runbooks/` — Operational guides (build-and-deploy, database-operations)
- Local `CLAUDE.md` files for all 5 modules
- README.md with project overview, setup instructions, and usage guide
- docs/architecture.md with system design, data flow diagrams, and module relationships
- CHANGELOG.md for tracking version history
- COMMIT_LOG.md for tracking commit logs and functional changes
- .gitignore for Java/Maven/IDE artifacts
- Unit tests for shared-utils (TimeUtils, Formatters, LotSizes, NseClient)
- Unit tests for shared-db model classes (StockAnalysis, MarketSnapshot)
- Unit tests for agent-market-analyst (AnalystSettings, TechnicalCollector, NseCollector)
- Unit tests for agent-trade-executor (TradeExecutorApplication)
- Unit tests for agent-learning-summary (LearningSummaryApplication)

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
