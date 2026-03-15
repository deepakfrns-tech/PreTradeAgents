# Commit Log & Functional Changes

This file tracks all commits and their functional impact on the PreTradeAgents system. Update this file with every significant commit.

## How to Use This File

When making changes, add an entry under the current date with:
- **Commit message**: Short description of the change
- **Files changed**: List of modified/added/deleted files
- **Functional impact**: What behavior changed, was added, or was removed
- **Breaking changes**: Any changes that require updates to other modules or configuration

---

## 2026-03-15

### Commit: Add CSV export, trade dashboard, execution scheduling, and learning services

**Files added:**
- `trade-dashboard/pom.xml` - New Spring Boot web module (port 8080)
- `trade-dashboard/src/main/java/com/pretrade/dashboard/TradeDashboardApplication.java` - Dashboard entry point
- `trade-dashboard/src/main/java/com/pretrade/dashboard/controller/DashboardController.java` - Upload, dashboard, trade approval
- `trade-dashboard/src/main/java/com/pretrade/dashboard/service/CsvParserService.java` - CSV parsing with quoted field support
- `trade-dashboard/src/main/java/com/pretrade/dashboard/db/StockAnalysisRepository.java` - Signal queries
- `trade-dashboard/src/main/java/com/pretrade/dashboard/db/TradeDecisionRepository.java` - Decision persistence
- `trade-dashboard/src/main/resources/templates/dashboard.html` - Signal dashboard with scoring, details, approval
- `trade-dashboard/src/main/resources/templates/upload.html` - CSV upload with drag-and-drop
- `trade-dashboard/src/main/resources/static/css/dashboard.css` - Dark theme styling
- `trade-dashboard/src/main/resources/application.yml` - Dashboard configuration
- `trade-dashboard/src/test/java/com/pretrade/dashboard/TradeDashboardApplicationTest.java`
- `trade-dashboard/src/test/java/com/pretrade/dashboard/CsvParserServiceTest.java` - CSV parsing tests
- `agent-market-analyst/src/main/java/com/pretrade/analyst/service/CsvExportService.java` - CSV generation
- `agent-market-analyst/src/main/java/com/pretrade/analyst/controller/AnalystController.java` - REST API
- `agent-market-analyst/src/main/java/com/pretrade/analyst/db/StockAnalysisRepository.java` - JPA repo
- `agent-trade-executor/src/main/java/com/pretrade/executor/service/TradeExecutionService.java` - 9:15 trigger + monitoring
- `agent-trade-executor/src/main/java/com/pretrade/executor/controller/ExecutorController.java` - REST API
- `agent-trade-executor/src/main/java/com/pretrade/executor/db/TradeDecisionRepository.java` - JPA repo
- `agent-trade-executor/src/main/java/com/pretrade/executor/db/PaperTradeRepository.java` - JPA repo
- `agent-learning-summary/src/main/java/com/pretrade/learner/service/LearningSummaryService.java` - Summary + patterns
- `agent-learning-summary/src/main/java/com/pretrade/learner/controller/LearnerController.java` - REST API
- `agent-learning-summary/src/main/java/com/pretrade/learner/db/PaperTradeRepository.java` - JPA repo
- `agent-learning-summary/src/main/java/com/pretrade/learner/db/DailySummaryRepository.java` - JPA repo
- `agent-learning-summary/src/main/java/com/pretrade/learner/db/StrategyLearningRepository.java` - JPA repo
- `agent-learning-summary/src/main/java/com/pretrade/learner/db/StockAnalysisRepository.java` - JPA repo
- `agent-learning-summary/src/main/java/com/pretrade/learner/db/TradeDecisionRepository.java` - JPA repo
- `scripts/run.sh` - Independent agent runner script

**Files modified:**
- `agent-market-analyst/src/main/java/com/pretrade/analyst/MarketAnalystApplication.java` - Added utils scan
- `agent-market-analyst/src/main/resources/application.yml` - Added csv-output-dir config
- `agent-trade-executor/src/main/java/com/pretrade/executor/TradeExecutorApplication.java` - Added utils scan
- `agent-learning-summary/src/main/java/com/pretrade/learner/LearningSummaryApplication.java` - Added utils scan
- `docker-compose.yml` - Added dashboard service, profiles, CSV volume
- `scripts/build.sh` - Added trade-dashboard, target module support
- `scripts/test.sh` - Added trade-dashboard to modules list
- `CHANGELOG.md` - Updated with all changes
- `COMMIT_LOG.md` - This entry

**Functional impact:**
- Market Analyst now exports signals to CSV (`trade-signals-YYYY-MM-DD.csv`)
- New Trade Dashboard web app at port 8080 for uploading CSV and approving trades
- Trade selections in the dashboard are persisted as TradeDecisions in the database
- Trade Executor automatically triggers paper trades at 9:15 AM IST for approved decisions
- Learning Summary generates DailySummary aggregations and mines strategy patterns
- All agents can run independently via `./scripts/run.sh {postgres|analyst|dashboard|executor|learner}`
- Docker Compose supports profiles for selective service startup

**Breaking changes:**
- `docker-compose.yml` now uses profiles — use `docker compose --profile all up` for full stack

---

### Commit: Add local deployment, mandatory doc-update rules, and build scripts

**Files added:**
- `docker-compose.yml` - Full local deployment (PostgreSQL + 3 agents)
- `agent-market-analyst/Dockerfile` - Multi-stage build for Agent 1
- `agent-trade-executor/Dockerfile` - Multi-stage build for Agent 2
- `agent-learning-summary/Dockerfile` - Multi-stage build for Agent 3
- `.dockerignore` - Docker build context optimization
- `.env.example` - Environment variable template
- `scripts/build.sh` - Build all modules in correct order
- `scripts/test.sh` - Run tests across all or specific modules
- `scripts/local-deploy.sh` - Docker Compose deployment wrapper (up/down/restart/logs/status)
- `.claude/skills/doc-update/SKILL.md` - Mandatory doc-update workflow
- `docs/runbooks/local-deployment.md` - Docker and manual deployment runbook

**Files modified:**
- `CLAUDE.md` - Added Docker commands, mandatory doc-update rules section
- `.claude/settings.json` - Added Docker/script permissions, pre-commit hook
- `.claude/hooks/README.md` - Rewritten with active hook documentation
- `README.md` - Added Quick Start (Docker), scripts table, deployment docs
- `.gitignore` - Added Docker entries
- `CHANGELOG.md` - Updated with all changes
- `COMMIT_LOG.md` - This entry

**Functional impact:**
- Project is now locally deployable with a single command (`./scripts/local-deploy.sh up`)
- Claude is now mandated to update docs (CHANGELOG, COMMIT_LOG, module CLAUDE.md) on every change
- Pre-commit hook reminds about doc updates before committing
- Build/test/deploy scripts eliminate manual multi-step processes

**Breaking changes:** None

---

### Commit: Apply AI-friendly best practices (tweet-inspired restructure)

**Files added:**
- `CLAUDE.md` - Rewritten: concise north-star (purpose, map, rules only)
- `.claude/settings.json` - Claude Code project permissions
- `.claude/skills/code-review/SKILL.md` - Code review expert mode
- `.claude/skills/refactor/SKILL.md` - Refactoring playbook
- `.claude/skills/debug/SKILL.md` - Debugging workflow
- `.claude/skills/release/SKILL.md` - Release procedure
- `.claude/hooks/README.md` - Guardrail documentation
- `docs/decisions/001-multi-agent-architecture.md` - ADR
- `docs/decisions/002-claude-ai-integration.md` - ADR
- `docs/decisions/003-no-parent-pom.md` - ADR
- `docs/runbooks/build-and-deploy.md` - Operational guide
- `docs/runbooks/database-operations.md` - Operational guide
- `shared-db/CLAUDE.md` - Local context for database module
- `shared-utils/CLAUDE.md` - Local context for utilities module
- `agent-market-analyst/CLAUDE.md` - Local context for analyst agent
- `agent-trade-executor/CLAUDE.md` - Local context for executor agent
- `agent-learning-summary/CLAUDE.md` - Local context for learner agent

**Functional impact:**
- No functional code changes. Structure and documentation only.
- CLAUDE.md reduced from 183 lines to ~65 lines (focused on purpose/map/rules)
- Detailed module context moved to per-module CLAUDE.md files
- ADRs document key architecture decisions for future contributors
- Skills provide consistent AI workflows across sessions
- Runbooks provide operational procedures for common tasks

**Breaking changes:** None

---

### Commit: Restructure repo for AI-friendly development

**Files added:**
- `CLAUDE.md` - AI assistant guide for codebase navigation
- `README.md` - Project documentation with setup and usage
- `docs/architecture.md` - System architecture and data flow documentation
- `CHANGELOG.md` - Version history tracking
- `COMMIT_LOG.md` - This file (commit and functional change tracking)
- `.gitignore` - Git ignore rules for Java/Maven/IDE artifacts
- `shared-utils/src/test/java/com/pretrade/utils/TimeUtilsTest.java`
- `shared-utils/src/test/java/com/pretrade/utils/FormattersTest.java`
- `shared-utils/src/test/java/com/pretrade/utils/LotSizesTest.java`
- `shared-utils/src/test/java/com/pretrade/utils/NseClientTest.java`
- `shared-db/src/test/java/com/pretrade/shared/models/StockAnalysisTest.java`
- `shared-db/src/test/java/com/pretrade/shared/models/MarketSnapshotTest.java`
- `agent-market-analyst/src/test/java/com/pretrade/analyst/config/AnalystSettingsTest.java`
- `agent-market-analyst/src/test/java/com/pretrade/analyst/collectors/TechnicalCollectorTest.java`
- `agent-market-analyst/src/test/java/com/pretrade/analyst/collectors/NseCollectorTest.java`
- `agent-trade-executor/src/test/java/com/pretrade/executor/TradeExecutorApplicationTest.java`
- `agent-learning-summary/src/test/java/com/pretrade/learner/LearningSummaryApplicationTest.java`

**Functional impact:**
- No functional code changes. Documentation and test infrastructure only.
- Test dependencies (`spring-boot-starter-test`) were already present in all agent POMs.
- Added `spring-boot-starter-test` dependency to `shared-utils/pom.xml` and `shared-db/pom.xml` for test support.

**Breaking changes:** None

---

### Commit: Initial commit - Pre-trade multi-agent system

**Files added:**
- All source files for the initial multi-agent trading system
- 3 Spring Boot agent applications
- 2 shared library modules
- 6 Flyway database migrations
- 4 utility classes
- 3 data collectors
- 6 JPA entity models

**Functional impact:**
- Complete multi-agent trading system foundation
- NSE pre-market data collection pipeline
- Claude AI-powered stock analysis scoring
- Paper trade execution framework
- Daily learning and strategy recommendation engine

**Breaking changes:** N/A (initial commit)

---

## Template for New Entries

```markdown
### Commit: <short description>

**Files changed:**
- `path/to/file.java` - Description of change

**Functional impact:**
- What behavior changed

**Breaking changes:**
- Any breaking changes (or "None")
```
