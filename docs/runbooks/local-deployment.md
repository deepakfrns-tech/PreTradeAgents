# Runbook: Local Deployment

## Option 1: Docker Compose (Recommended)

One command to start everything — PostgreSQL + all 3 agents.

### Prerequisites
- Docker and Docker Compose installed
- (Optional) Anthropic API key for AI features

### Quick Start

```bash
# 1. Set up environment
cp .env.example .env
# Edit .env — add your ANTHROPIC_API_KEY

# 2. Start everything
./scripts/local-deploy.sh up

# 3. Verify
./scripts/local-deploy.sh status
```

### Services

| Service | URL | Container |
|---------|-----|-----------|
| PostgreSQL | localhost:5432 | pretrade-db |
| Market Analyst | http://localhost:8081 | pretrade-market-analyst |
| Trade Executor | http://localhost:8082 | pretrade-trade-executor |
| Learning Summary | http://localhost:8083 | pretrade-learning-summary |

### Commands

```bash
./scripts/local-deploy.sh up       # Build and start all
./scripts/local-deploy.sh down     # Stop all (data preserved)
./scripts/local-deploy.sh restart  # Rebuild and restart
./scripts/local-deploy.sh logs     # Tail all logs
./scripts/local-deploy.sh status   # Show running services
```

### Data Persistence
- PostgreSQL data is stored in a Docker volume (`pretrade-pgdata`)
- `docker compose down` preserves data
- `docker compose down -v` deletes data (full reset)

---

## Option 2: Manual (No Docker)

### Prerequisites
- Java 21 JDK
- Maven 3.8+
- PostgreSQL 15+ running locally

### Steps

```bash
# 1. Database
createdb pretrade

# 2. Environment
export DB_HOST=localhost DB_PORT=5432 DB_NAME=pretrade
export DB_USERNAME=pretrade DB_PASSWORD=pretrade
export ANTHROPIC_API_KEY=sk-ant-...

# 3. Build
./scripts/build.sh

# 4. Run (each in a separate terminal)
java -jar agent-market-analyst/target/agent-market-analyst-1.0.0-SNAPSHOT.jar
java -jar agent-trade-executor/target/agent-trade-executor-1.0.0-SNAPSHOT.jar
java -jar agent-learning-summary/target/agent-learning-summary-1.0.0-SNAPSHOT.jar
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Port 5432 in use | Stop existing PostgreSQL or change port in docker-compose.yml |
| Port 8081/82/83 in use | Stop existing process or change in application.yml |
| Docker build fails | Check Docker daemon is running; try `docker compose build --no-cache` |
| DB connection refused | Wait for PostgreSQL healthcheck; check `docker compose logs postgres` |
| "shared-db not found" (manual) | Run `cd shared-db && mvn clean install` first |
| Flyway migration fails | Check if schema already exists; use `docker compose down -v` to reset |
