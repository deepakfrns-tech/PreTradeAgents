# CLAUDE.md - AI Assistant Guide for PreTradeAgents

This file provides context for AI assistants (Claude, Copilot, etc.) working on this codebase.

## Project Overview

**PreTradeAgents** is a multi-agent, Claude-powered pre-market analysis and paper trading system for the NSE (National Stock Exchange of India). It uses three Spring Boot microservices that work together to analyze markets, execute paper trades, and extract strategy learnings.

## Tech Stack

- **Language**: Java 21 (agents), Java 17 (shared libs)
- **Framework**: Spring Boot 3.2.3
- **Database**: PostgreSQL with Flyway migrations
- **ORM**: JPA/Hibernate with Lombok
- **AI**: Anthropic Claude API (Sonnet for agents 1&2, Opus for agent 3)
- **HTTP**: Spring WebClient (reactive) for NSE API, RestTemplate in shared-utils
- **Build**: Maven (no parent POM - each module builds independently)

## Repository Structure

```
PreTradeAgents/
├── CLAUDE.md                    # THIS FILE - AI assistant guide
├── README.md                    # Project documentation
├── CHANGELOG.md                 # Version history and changes
├── COMMIT_LOG.md                # Commit logs and functional changes
├── .gitignore                   # Git ignore rules
├── docs/
│   └── architecture.md          # System architecture documentation
├── agent-market-analyst/        # Agent 1 - Pre-market data collection & AI analysis
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/pretrade/analyst/
│       │   ├── MarketAnalystApplication.java    # Entry point (port 8081)
│       │   ├── config/AnalystSettings.java      # Config properties
│       │   └── collectors/
│       │       ├── NseCollector.java             # NSE pre-market & option chain data
│       │       ├── TechnicalCollector.java       # Volume & VWAP analysis
│       │       └── NewsCollector.java            # Google News & MoneyControl scraping
│       └── test/java/com/pretrade/analyst/      # Unit tests
├── agent-trade-executor/        # Agent 2 - Paper trade execution & monitoring
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/pretrade/executor/
│       │   └── TradeExecutorApplication.java    # Entry point (port 8082)
│       └── test/java/com/pretrade/executor/     # Unit tests
├── agent-learning-summary/      # Agent 3 - Daily summaries & strategy learning
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/pretrade/learner/
│       │   └── LearningSummaryApplication.java  # Entry point (port 8083)
│       └── test/java/com/pretrade/learner/      # Unit tests
├── shared-db/                   # Shared JPA entity models & Flyway migrations
│   ├── pom.xml
│   ├── migrations/              # Flyway SQL migrations (V001-V006)
│   └── src/
│       ├── main/java/com/pretrade/shared/models/
│       │   ├── MarketSnapshot.java              # Market conditions at pre-open
│       │   ├── StockAnalysis.java               # Core trading signal with scoring
│       │   ├── TradeDecision.java               # Approval/rejection of signals
│       │   ├── PaperTrade.java                  # Simulated trade execution records
│       │   ├── DailySummary.java                # End-of-day performance aggregation
│       │   └── StrategyLearning.java            # Discovered trading patterns
│       └── test/java/com/pretrade/shared/models/ # Unit tests
└── shared-utils/                # Shared utility classes
    ├── pom.xml
    └── src/
        ├── main/java/com/pretrade/utils/
        │   ├── NseClient.java                   # NSE API HTTP client with retry logic
        │   ├── TimeUtils.java                   # IST timezone & market hours utilities
        │   ├── Formatters.java                  # INR/percentage/PnL formatters
        │   └── LotSizes.java                    # F&O lot size registry (50+ symbols)
        └── test/java/com/pretrade/utils/        # Unit tests
```

## Module Dependency Graph

```
agent-market-analyst ──┐
agent-trade-executor ──┼──> shared-db + shared-utils
agent-learning-summary─┘
```

All three agents depend on `shared-db` (JPA models) and `shared-utils` (utilities). There are no inter-agent dependencies.

## Build & Run

```bash
# Build shared libraries first, then agents
cd shared-db && mvn clean install && cd ..
cd shared-utils && mvn clean install && cd ..
cd agent-market-analyst && mvn clean package && cd ..
cd agent-trade-executor && mvn clean package && cd ..
cd agent-learning-summary && mvn clean package && cd ..

# Run tests for a specific module
cd shared-utils && mvn test

# Run a specific agent
java -jar agent-market-analyst/target/agent-market-analyst-1.0.0-SNAPSHOT.jar
```

## Required Environment Variables

```bash
DB_HOST=localhost          # PostgreSQL host
DB_PORT=5432               # PostgreSQL port
DB_NAME=pretrade           # Database name
DB_USERNAME=pretrade       # Database user
DB_PASSWORD=pretrade       # Database password
ANTHROPIC_API_KEY=sk-ant-... # Claude API key (required for AI analysis)
```

## Key Conventions

1. **Package naming**: `com.pretrade.<module>` (e.g., `com.pretrade.analyst`, `com.pretrade.executor`)
2. **Config classes**: Use `@ConfigurationProperties` with prefix matching module name
3. **DTOs**: Defined as static inner classes within their parent service class (using Lombok `@Data @Builder`)
4. **Database**: All entities in `shared-db`, migrations in `shared-db/migrations/V00N__*.sql`
5. **Timestamps**: Always use IST (`Asia/Kolkata`) via `TimeUtils`
6. **Currency**: Always use Indian numbering system (lakhs/crores) via `Formatters`
7. **Lot sizes**: Validated through `LotSizes` registry before trade execution

## Database Schema (6 tables)

| Table | Model Class | Purpose |
|-------|-------------|---------|
| `market_snapshots` | `MarketSnapshot` | Pre-market conditions (VIX, gap%, A/D ratio) |
| `stock_analysis` | `StockAnalysis` | AI-scored trading signals (composite score, entry params) |
| `trade_decisions` | `TradeDecision` | Approval/rejection with optional overrides |
| `paper_trades` | `PaperTrade` | Simulated execution with PnL tracking |
| `daily_summaries` | `DailySummary` | EOD aggregation (win rate, Sharpe, patterns) |
| `strategy_learnings` | `StrategyLearning` | Discovered patterns with confidence scores |

## Agent Ports

| Agent | Port | Schedule |
|-------|------|----------|
| Market Analyst | 8081 | 9:00-9:15 AM IST (pre-market) |
| Trade Executor | 8082 | 9:15 AM-3:30 PM IST (market hours) |
| Learning Summary | 8083 | ~4:00 PM IST (post-market) |

## Common Tasks for AI Assistants

### Adding a new data collector
1. Create a new `@Service` class in `agent-market-analyst/src/main/java/com/pretrade/analyst/collectors/`
2. Inject `AnalystSettings` for configuration
3. Define DTOs as static inner classes with `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
4. Add corresponding tests

### Adding a new database entity
1. Create the entity in `shared-db/src/main/java/com/pretrade/shared/models/`
2. Create a Flyway migration in `shared-db/migrations/V00N__description.sql`
3. Use `@Entity`, `@Table`, `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
4. Add corresponding tests

### Adding a new F&O stock
1. Edit `shared-utils/src/main/java/com/pretrade/utils/LotSizes.java`
2. Add `map.put("SYMBOL", lotSize)` in the static initializer block
3. Update the corresponding test

### Adding configuration properties
1. Add to the relevant `application.yml` file
2. Add a field in the corresponding `*Settings.java` class
3. Use `@ConfigurationProperties(prefix = "module-name")`

## Testing

- Tests use JUnit 5 (via `spring-boot-starter-test`)
- Test files follow the pattern `<ClassName>Test.java`
- Shared-utils tests: pure unit tests (no Spring context needed)
- Agent tests: may need `@SpringBootTest` or `@MockBean` for integration
- Run all tests: `mvn test` in each module directory

## Things to Watch Out For

- **No parent POM**: Each module is an independent Maven project. Build `shared-db` and `shared-utils` first.
- **NSE rate limiting**: NSE blocks rapid API calls. Always warm up session first (hit base URL).
- **IST timezone**: All time logic must use `Asia/Kolkata`, never system default timezone.
- **JSONB columns**: `headlineDetails`, `fiiDiiData`, `rawPreMarketData`, `priceTrail`, `slAdjustments`, `tradePostmortems`, etc. are stored as JSONB in PostgreSQL.
- **Lombok**: All model/DTO classes use Lombok annotations. Ensure Lombok plugin is installed in your IDE.
- **Java version mismatch**: Shared libs use Java 17, agents use Java 21 (switch statements, etc.).
