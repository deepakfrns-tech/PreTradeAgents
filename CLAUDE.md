# CLAUDE.md

## Purpose

PreTradeAgents is a multi-agent Claude-powered trading system for NSE (India). Three Python/Flask agents + a web dashboard analyze pre-market data, execute paper trades, and extract strategy learnings — all coordinated through a shared PostgreSQL database.

## Repo Map

```
PreTradeAgents/
├── shared/                      # Shared library — models, DB, time_utils, formatters, lot_sizes, nse_client
├── market_analyst/              # Agent 1 (port 8081) — NSE data collectors + Claude scoring + CSV export
│   └── collectors/              # NseCollector, NewsCollector, TechnicalCollector
├── trade_dashboard/             # Web Dashboard (port 8080) — Upload CSV, view signals, approve trades
│   ├── templates/               # Jinja2 templates (base, dashboard, upload)
│   └── static/css/              # Dark-themed responsive styling
├── trade_executor/              # Agent 2 (port 8082) — Paper trade execution at 9:15 AM
├── learning_summary/            # Agent 3 (port 8083) — Pattern mining + strategy learnings
├── shared-db/migrations/        # SQL migrations (7 migrations, applied in order)
├── tests/                       # pytest test suite
├── docs/                        # Architecture, ADRs, runbooks
├── scripts/                     # run.sh, build.sh, test.sh
├── Dockerfile.*                 # Per-service Dockerfiles
├── docker-compose.yml           # Local deployment with profiles
├── requirements.txt             # Python dependencies
└── .claude/                     # Claude Code settings, skills, hooks
```

## Rules

### MUST
- All timestamps via `shared.time_utils` (IST / Asia/Kolkata) — never `datetime.now()` directly
- All currency via `shared.formatters` (Indian numbering: lakhs/crores)
- Validate F&O symbols via `shared.lot_sizes` before any trade logic
- New DB changes → create migration in `shared-db/migrations/V00N__*.sql`
- Run `python -m pytest tests/` before committing

### MUST NOT
- Never modify migrations that have already been applied
- Never hardcode API keys — use environment variables
- Never use system default timezone — always `Asia/Kolkata`
- Never call NSE API without session warmup (hit base URL first)
- Never create inter-agent dependencies — agents communicate only through the database

### MANDATORY: Update Docs on Every Change

**After every code change, you MUST update these files before committing:**

1. **`CHANGELOG.md`** — Add entry under `[Unreleased]` describing what changed
2. **`COMMIT_LOG.md`** — Add entry with: files changed, functional impact, breaking changes
3. **`docs/architecture.md`** — If you changed system design, data flow, or module relationships

### Environment Variables

```
DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, ANTHROPIC_API_KEY, CSV_OUTPUT_DIR
```

## Commands

```bash
# Setup
pip install -r requirements.txt

# Test
python -m pytest tests/ -v
./scripts/test.sh

# Run independently (each in separate terminal)
./scripts/run.sh postgres       # Start PostgreSQL via Docker
./scripts/run.sh analyst        # Market Analyst (port 8081)
./scripts/run.sh dashboard      # Trade Dashboard (port 8080)
./scripts/run.sh executor       # Trade Executor (port 8082)
./scripts/run.sh learner        # Learning Summary (port 8083)

# Docker deployment with profiles
docker compose --profile all up -d           # Start everything
docker compose --profile dashboard up -d     # Start only dashboard + postgres
docker compose down                          # Stop all services
```
