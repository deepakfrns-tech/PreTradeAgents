# Commit Log & Functional Changes

This file tracks all commits and their functional impact on the PreTradeAgents system. Update this file with every significant commit.

## How to Use This File

When making changes, add an entry under the current date with:
- **Commit message**: Short description of the change
- **Files changed**: List of modified/added/deleted files
- **Functional impact**: What behavior changed, was added, or was removed
- **Breaking changes**: Any changes that require updates to other modules or configuration

---

## 2026-03-16

### Commit: Add root route to Market Analyst agent to fix 404 on /

**Files modified:**
- `market_analyst/app.py` — Added `/` route returning agent info and endpoint listing as JSON

**Functional impact:**
- Hitting `http://127.0.0.1:8081/` now returns a JSON response with agent status and available endpoints instead of a 404 error

**Breaking changes:** None

---

### Commit: Update all documentation to reference Python/Flask instead of Java/Spring Boot

**Files modified:**
- `README.md` — Rewritten: Python prereqs, pip install, python -m commands, updated project structure
- `docs/architecture.md` — Rewritten: Python module structure, SQLAlchemy models, Flask apps, env var config
- `docs/runbooks/local-deployment.md` — Rewritten: Python prereqs, pip install, python -m commands
- `docs/runbooks/build-and-deploy.md` — Rewritten: pip install, python runners, Docker deployment
- `docs/runbooks/database-operations.md` — Rewritten: SQLAlchemy models, SQL migrations, Python paths
- `docs/decisions/001-multi-agent-architecture.md` — Updated: Python/Flask agents
- `docs/decisions/003-no-parent-pom.md` — Rewritten: Shared library pattern for Python
- `docs/decisions/004-trade-dashboard-csv-workflow.md` — Updated: Flask + Jinja2
- `.claude/skills/release/SKILL.md` — Rewritten: pytest, pip, Python workflow
- `.claude/skills/refactor/SKILL.md` — Rewritten: Python refactoring rules
- `.claude/skills/debug/SKILL.md` — Rewritten: Python debugging workflow
- `.claude/skills/code-review/SKILL.md` — Rewritten: Python code review checklist
- `.claude/skills/doc-update/SKILL.md` — Updated: Python file paths and commands
- `.claude/hooks/README.md` — Updated: Python import checks, pytest commands

**Functional impact:**
- All documentation, runbooks, ADRs, and skills now accurately describe the Python/Flask codebase
- No more references to Java, Maven, JDK, JARs, Spring Boot, JPA, Lombok, Thymeleaf in active documentation

**Breaking changes:** None (documentation only)

---

## 2026-03-15

### Commit: Complete Java to Python refactor — migrate entire codebase

**Files added:**
- `shared/{__init__,database,models,time_utils,formatters,lot_sizes,nse_client}.py` — Shared utilities
- `market_analyst/{__init__,app,csv_export}.py` — Market Analyst agent
- `market_analyst/collectors/{__init__,nse_collector,news_collector,technical_collector}.py` — Data collectors
- `trade_dashboard/{__init__,app,csv_parser}.py` — Dashboard Flask app
- `trade_dashboard/templates/{base,dashboard,upload}.html` — Jinja2 templates
- `trade_dashboard/static/css/dashboard.css` — Dark-themed dashboard styling
- `trade_executor/{__init__,app}.py` — Trade Executor with 9:15 AM scheduler
- `learning_summary/{__init__,app}.py` — Learning Summary with pattern mining
- `tests/test_{time_utils,formatters,lot_sizes,technical_collector,csv_parser}.py` — 40 pytest tests
- `requirements.txt` — Python dependencies
- `Dockerfile.{analyst,dashboard,executor,learner}` — Per-service Python Dockerfiles

**Files removed:**
- All Java source code under `agent-market-analyst/src/`, `agent-trade-executor/src/`, `agent-learning-summary/src/`, `trade-dashboard/src/`, `shared-db/src/`, `shared-utils/src/`
- All Maven POM files
- All Spring Boot Dockerfiles
- `py/` subdirectory (code promoted to root level)

**Files modified:**
- `docker-compose.yml` — Updated for Python Dockerfiles
- `scripts/run.sh` — Rewritten for Python agents
- `scripts/build.sh` — Rewritten for Python (pip install + import verification)
- `scripts/test.sh` — Rewritten for pytest
- `.gitignore` — Updated for Python artifacts
- `CLAUDE.md` — Rewritten for Python codebase
- `CHANGELOG.md` — Updated with refactor details
- `COMMIT_LOG.md` — This entry

**Functional impact:**
- Complete technology migration: Java/Spring Boot/Maven → Python/Flask/pip
- Same database schema — Flyway migrations preserved in `shared-db/migrations/`
- Same REST API endpoints — all agents maintain identical HTTP interfaces
- Same ports: 8080 (dashboard), 8081 (analyst), 8082 (executor), 8083 (learner)
- New: NseClient, NseCollector, NewsCollector, TechnicalCollector in Python
- New: 40 pytest tests covering shared utilities and CSV parsing

**Breaking changes:**
- Java build tools (Maven, JDK) no longer required
- Python 3.11+ and pip now required
- Docker images changed from JRE-alpine to python:3.12-slim
- Build/test/run scripts completely rewritten for Python

---

### Commit: Add local execution guide for Python agents

**Files added:**
- `py/LOCAL_SETUP.md` - Step-by-step local setup and execution guide for all 4 Python agents

**Functional impact:**
- Developers now have a single reference document covering prerequisites, DB setup, running each agent, endpoint reference, daily workflow, and troubleshooting

**Breaking changes:** None

---

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

### Commit: Add Python + Flask reimplementation of all agents

**Files added:**
- `py/requirements.txt` — Python dependencies (Flask, SQLAlchemy, APScheduler, etc.)
- `py/shared/{__init__,database,models,time_utils,formatters,lot_sizes}.py` — Shared utilities matching Java/DB schema
- `py/market_analyst/{__init__,app,csv_export}.py` — Market Analyst with CSV export
- `py/trade_dashboard/{__init__,app,csv_parser}.py` — Dashboard Flask app
- `py/trade_dashboard/templates/{base,dashboard,upload}.html` — Jinja2 templates
- `py/trade_dashboard/static/css/dashboard.css` — Dark-themed dashboard styling
- `py/trade_executor/{__init__,app}.py` — Trade Executor with 9:15 AM scheduler
- `py/learning_summary/{__init__,app}.py` — Learning Summary with pattern mining
- `py/run.sh` — Independent runner script for each agent

**Functional impact:**
- Full Python alternative to Java agents — same DB schema, same functionality
- Each agent runs independently as a standalone Flask app
- All imports verified, Flask apps start and serve correctly
- CSV export/import round-trip tested
- APScheduler triggers at 9:15 AM IST on weekdays

**Breaking changes:** None — Python version is additive, Java code unchanged

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
- `path/to/file.py` - Description of change

**Functional impact:**
- What behavior changed

**Breaking changes:**
- Any breaking changes (or "None")
```
