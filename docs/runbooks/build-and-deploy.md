# Runbook: Build and Deploy

## Install Dependencies

```bash
pip install -r requirements.txt
```

## Verify All Modules

```bash
./scripts/build.sh
```

This installs dependencies and verifies that all module imports work correctly.

## Run All Agents

```bash
# Terminal 1: Start PostgreSQL + apply migrations
./scripts/run.sh postgres

# Terminal 2
export DB_HOST=localhost DB_PORT=5432 DB_NAME=pretrade DB_USERNAME=pretrade DB_PASSWORD=pretrade
export ANTHROPIC_API_KEY=sk-ant-...
python -m market_analyst.app

# Terminal 3 (same env vars)
python -m trade_dashboard.app

# Terminal 4 (same env vars)
python -m trade_executor.app

# Terminal 5 (same env vars)
python -m learning_summary.app
```

## Run a Single Agent

```bash
./scripts/run.sh analyst     # or dashboard, executor, learner
```

## Database Setup

```bash
# Start PostgreSQL
./scripts/run.sh postgres

# Or apply migrations to existing PostgreSQL
./scripts/run.sh init-db
```

## Docker Deployment

```bash
# Build all images
./scripts/build.sh docker

# Start everything
./scripts/local-deploy.sh up

# Start specific profile
docker compose --profile dashboard up -d
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| ImportError on shared module | Run `pip install -r requirements.txt` from project root |
| DB connection refused | Check PostgreSQL is running, verify DB_HOST/DB_PORT |
| NSE API timeout | Normal outside market hours; check internet connectivity |
| Port already in use | Kill existing process: `lsof -i :PORT` then `kill PID` |
