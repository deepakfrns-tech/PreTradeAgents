# Changelog

All notable changes to the PreTradeAgents project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- CLAUDE.md rewritten to be concise (purpose, repo map, rules only) per best practices
- `.claude/settings.json` with permissions (allowed/denied commands)
- `.claude/skills/` — 4 reusable AI workflow skills:
  - `code-review/SKILL.md` — code review checklist with project-specific rules
  - `refactor/SKILL.md` — safe refactoring playbook with pre/post checklists
  - `debug/SKILL.md` — systematic debugging workflow with common failure patterns
  - `release/SKILL.md` — release procedure with build verification steps
- `.claude/hooks/README.md` — guardrail documentation for automated checks
- `docs/decisions/` — Architecture Decision Records (ADRs):
  - ADR-001: Multi-agent microservice architecture
  - ADR-002: Claude AI for analysis and learning
  - ADR-003: Independent Maven modules (no parent POM)
- `docs/runbooks/` — Operational guides:
  - `build-and-deploy.md` — full build, run, and troubleshooting
  - `database-operations.md` — schema changes, JSONB queries, migration rules
- Local `CLAUDE.md` files for each critical module:
  - `shared-db/CLAUDE.md` — migration danger zones, entity-schema mapping
  - `shared-utils/CLAUDE.md` — utility class rules and common changes
  - `agent-market-analyst/CLAUDE.md` — collector architecture, scoring weights
  - `agent-trade-executor/CLAUDE.md` — trade lifecycle, JSONB fields
  - `agent-learning-summary/CLAUDE.md` — Claude Opus usage, lookback queries

### Changed
- CLAUDE.md restructured from verbose guide to focused north-star document
- Previous detailed content moved to docs/architecture.md and local CLAUDE.md files

### Previously Added
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
