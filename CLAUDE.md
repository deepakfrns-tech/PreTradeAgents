# CLAUDE.md

## Purpose

PreTradeAgents is a multi-agent Claude-powered trading system for NSE (India). Three Spring Boot agents + a web dashboard analyze pre-market data, execute paper trades, and extract strategy learnings — all coordinated through a shared PostgreSQL database.

## Repo Map

```
PreTradeAgents/
├── agent-market-analyst/        # Agent 1 (port 8081) - NSE data + Claude scoring + CSV export
├── trade-dashboard/             # Web Dashboard (port 8080) - Upload CSV, view signals, approve trades
├── agent-trade-executor/        # Agent 2 (port 8082) - Paper trade execution at 9:15 AM
├── agent-learning-summary/      # Agent 3 (port 8083) - Pattern mining + strategy learnings
├── shared-db/                   # JPA entities + Flyway migrations (6 tables)
├── shared-utils/                # NseClient, TimeUtils, Formatters, LotSizes
├── docs/                        # Architecture, ADRs, runbooks (progressive context)
│   ├── architecture.md
│   ├── decisions/               # Architecture Decision Records
│   └── runbooks/                # Operational guides
├── docker-compose.yml           # Local deployment with profiles
├── scripts/                     # Build, test, run, and deployment scripts
└── .claude/
    ├── settings.json            # Claude Code project settings
    ├── skills/                  # Reusable AI workflows
    └── hooks/                   # Automated guardrails
```

Each module has its own `CLAUDE.md` with module-specific context.

## Rules

### MUST
- Build order: `shared-db` → `shared-utils` → agents (no parent POM)
- All timestamps via `TimeUtils` (IST / Asia/Kolkata) — never system default
- All currency via `Formatters` (Indian numbering: lakhs/crores)
- Validate F&O symbols via `LotSizes` before any trade logic
- New DB entities → create Flyway migration in `shared-db/migrations/V00N__*.sql`
- DTOs as static inner classes with `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
- Run `mvn test` in affected modules before committing

### MUST NOT
- Never modify Flyway migrations that have already been applied
- Never hardcode API keys — use environment variables
- Never use system default timezone — always `Asia/Kolkata`
- Never call NSE API without session warmup (hit base URL first)
- Never create inter-agent dependencies — agents communicate only through the database

### MANDATORY: Update Docs on Every Change

**After every code change, you MUST update these files before committing:**

1. **`CHANGELOG.md`** — Add entry under `[Unreleased]` describing what changed (Added/Changed/Fixed/Removed)
2. **`COMMIT_LOG.md`** — Add entry with: files changed, functional impact, breaking changes
3. **Module `CLAUDE.md`** — If you changed a module's behavior, update its local CLAUDE.md
4. **`docs/architecture.md`** — If you changed system design, data flow, or module relationships
5. **`docs/decisions/`** — If you made a significant architecture decision, create a new ADR
6. **`docs/runbooks/`** — If you changed build, deploy, or operational procedures

**This is not optional.** Stale docs are worse than no docs. Update them as part of the same commit.

### Environment Variables

```
DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, ANTHROPIC_API_KEY, CSV_OUTPUT_DIR
```

## Commands

```bash
# Build everything
./scripts/build.sh

# Test everything
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
docker compose --profile executor up -d      # Start only executor + postgres
docker compose down                          # Stop all services

# Build + run without Docker
cd shared-db && mvn clean install && cd .. && cd shared-utils && mvn clean install && cd ..
cd trade-dashboard && mvn clean package && cd ..
java -jar trade-dashboard/target/trade-dashboard-1.0.0-SNAPSHOT.jar

# Test a single module
cd <module> && mvn test
```
