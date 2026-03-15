# Changelog

All notable changes to the PreTradeAgents project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- CLAUDE.md for AI assistant guidance and codebase navigation
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
