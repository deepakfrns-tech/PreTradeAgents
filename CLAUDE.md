# CLAUDE.md

## Purpose

PreTradeAgents is a multi-agent Claude-powered trading system for NSE (India). Three Spring Boot microservices analyze pre-market data, execute paper trades, and extract strategy learnings — all coordinated through a shared PostgreSQL database.

## Repo Map

```
PreTradeAgents/
├── agent-market-analyst/        # Agent 1 (port 8081) - NSE data + Claude scoring
├── agent-trade-executor/        # Agent 2 (port 8082) - Paper trade execution
├── agent-learning-summary/      # Agent 3 (port 8083) - Pattern mining + strategy
├── shared-db/                   # JPA entities + Flyway migrations (6 tables)
├── shared-utils/                # NseClient, TimeUtils, Formatters, LotSizes
├── docs/                        # Architecture, ADRs, runbooks (progressive context)
│   ├── architecture.md
│   ├── decisions/               # Architecture Decision Records
│   └── runbooks/                # Operational guides
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

### Environment Variables

```
DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, ANTHROPIC_API_KEY
```

## Commands

```bash
# Build everything
cd shared-db && mvn clean install && cd .. && cd shared-utils && mvn clean install && cd .. && cd agent-market-analyst && mvn clean package && cd .. && cd agent-trade-executor && mvn clean package && cd .. && cd agent-learning-summary && mvn clean package && cd ..

# Test a module
cd <module> && mvn test

# Run an agent
java -jar <module>/target/<module>-1.0.0-SNAPSHOT.jar
```
