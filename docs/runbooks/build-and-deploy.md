# Runbook: Build and Deploy

## Full Build (All Modules)

```bash
# Build order matters — shared libs first
cd shared-db && mvn clean install && cd ..
cd shared-utils && mvn clean install && cd ..
cd agent-market-analyst && mvn clean package && cd ..
cd agent-trade-executor && mvn clean package && cd ..
cd agent-learning-summary && mvn clean package && cd ..
```

## Run All Agents

```bash
# Terminal 1
export DB_HOST=localhost DB_PORT=5432 DB_NAME=pretrade DB_USERNAME=pretrade DB_PASSWORD=pretrade
export ANTHROPIC_API_KEY=sk-ant-...
java -jar agent-market-analyst/target/agent-market-analyst-1.0.0-SNAPSHOT.jar

# Terminal 2 (same env vars)
java -jar agent-trade-executor/target/agent-trade-executor-1.0.0-SNAPSHOT.jar

# Terminal 3 (same env vars)
java -jar agent-learning-summary/target/agent-learning-summary-1.0.0-SNAPSHOT.jar
```

## Run a Single Agent

```bash
cd <module> && mvn spring-boot:run
```

## Database Setup

```bash
createdb pretrade
# Flyway migrations run automatically on first agent startup
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `shared-db` not found | Run `cd shared-db && mvn clean install` |
| DB connection refused | Check PostgreSQL is running, verify DB_HOST/DB_PORT |
| NSE API timeout | Normal outside market hours; check internet connectivity |
| Port already in use | Kill existing process or change port in application.yml |
