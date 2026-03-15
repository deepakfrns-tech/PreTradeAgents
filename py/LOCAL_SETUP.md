# Local Setup & Execution Guide

Step-by-step instructions for running each PreTrade Python agent locally.

## Prerequisites

- **Python 3.10+** (`python3 --version`)
- **PostgreSQL 14+** (via Docker or native install)
- **Docker** (optional, for PostgreSQL only)

## 1. Install Dependencies

```bash
cd py
pip install -r requirements.txt
```

Or using the run script:

```bash
./py/run.sh setup
```

## 2. Start PostgreSQL

### Option A: Docker (recommended)

```bash
./py/run.sh postgres
```

This starts PostgreSQL on port 5432 and applies all migrations automatically.

### Option B: Existing PostgreSQL

If you already have PostgreSQL running, set these environment variables:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=pretrade
export DB_USERNAME=pretrade
export DB_PASSWORD=pretrade
```

Then apply migrations:

```bash
./py/run.sh init-db
```

### Option C: Manual Docker

```bash
docker run -d --name pretrade-db \
  -e POSTGRES_DB=pretrade \
  -e POSTGRES_USER=pretrade \
  -e POSTGRES_PASSWORD=pretrade \
  -p 5432:5432 \
  postgres:16-alpine

# Wait a few seconds, then apply migrations
./py/run.sh init-db
```

## 3. Run Each Agent

Each agent runs independently in its own terminal. They communicate only through the shared PostgreSQL database.

---

### Agent 1: Market Analyst (port 8081)

Exports stock analysis signals to CSV files.

```bash
# Terminal 1
./py/run.sh analyst
```

**Verify:** http://localhost:8081/api/analyst/health

**Key endpoints:**
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/analyst/health` | Health check |
| GET | `/api/analyst/signals/<date>` | Get signals for a date (YYYY-MM-DD) |
| POST | `/api/analyst/export-csv?date=2026-03-15` | Export signals to CSV file |

**Environment variables:**
| Variable | Default | Description |
|----------|---------|-------------|
| `CSV_OUTPUT_DIR` | `/tmp/pretrade-csv` | Directory for exported CSV files |
| `PORT` | `8081` | Server port |

**Output:** Creates `trade-signals-YYYY-MM-DD.csv` in the CSV output directory.

---

### Agent 2: Trade Dashboard (port 8080)

Web UI for uploading CSV, viewing signals, and approving trades.

```bash
# Terminal 2
./py/run.sh dashboard
```

**Verify:** http://localhost:8080

**How to use:**
1. Open http://localhost:8080 in your browser
2. Click "Upload CSV" and upload the CSV file from Agent 1
3. Review signals on the dashboard — scores, direction, Claude reasoning, risk warnings
4. Check the trades you want to execute
5. Click "Approve Selected Trades" — selections are saved as TradeDecisions in the DB

**Environment variables:**
| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8080` | Server port |

---

### Agent 3: Trade Executor (port 8082)

Triggers paper trades at 9:15 AM IST for approved decisions.

```bash
# Terminal 3
./py/run.sh executor
```

**Verify:** http://localhost:8082/api/executor/health

**Key endpoints:**
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/executor/health` | Health check |
| POST | `/api/executor/trigger` | Manually trigger trade execution (skip waiting for 9:15 AM) |
| GET | `/api/executor/trades/<date>` | Get trades for a date |

**Automatic behavior:**
- At **9:15 AM IST** on weekdays, reads all APPROVED TradeDecisions and creates PaperTrade entries
- Every **30 seconds**, monitors open positions for stop-loss hits and EOD exit (3:15 PM IST)

**Environment variables:**
| Variable | Default | Description |
|----------|---------|-------------|
| `MAX_POSITIONS` | `5` | Maximum simultaneous positions |
| `PORT` | `8082` | Server port |

**Manual trigger:** If you don't want to wait until 9:15 AM, use:
```bash
curl -X POST http://localhost:8082/api/executor/trigger
```

---

### Agent 4: Learning Summary (port 8083)

Generates daily summaries and mines strategy patterns from trade history.

```bash
# Terminal 4
./py/run.sh learner
```

**Verify:** http://localhost:8083/api/learner/health

**Key endpoints:**
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/learner/health` | Health check |
| POST | `/api/learner/generate-summary?date=2026-03-15` | Generate daily summary |
| POST | `/api/learner/mine-patterns?date=2026-03-15` | Mine strategy patterns |
| GET | `/api/learner/summary/<date>` | Get summary for a date |
| GET | `/api/learner/learnings` | Get all active strategy learnings |

**Environment variables:**
| Variable | Default | Description |
|----------|---------|-------------|
| `MIN_CONFIDENCE` | `0.7` | Minimum confidence for pattern mining |
| `LOOKBACK_DAYS` | `30` | Days of history to analyze |
| `PATTERN_MIN_OCCURRENCES` | `3` | Minimum trades needed for pattern |
| `PORT` | `8083` | Server port |

---

## Typical Daily Workflow

```
1. Start PostgreSQL          →  ./py/run.sh postgres
2. Start Dashboard           →  ./py/run.sh dashboard
3. Run Analyst export        →  ./py/run.sh analyst
                                curl -X POST http://localhost:8081/api/analyst/export-csv
4. Upload CSV in browser     →  http://localhost:8080/upload
5. Review & approve trades   →  http://localhost:8080/dashboard
6. Start Executor            →  ./py/run.sh executor
   (or manual trigger)       →  curl -X POST http://localhost:8082/api/executor/trigger
7. After market close, run   →  ./py/run.sh learner
   learner                      curl -X POST http://localhost:8083/api/learner/generate-summary
                                curl -X POST http://localhost:8083/api/learner/mine-patterns
```

## Troubleshooting

**"Connection refused" on PostgreSQL:**
- Check PostgreSQL is running: `docker ps` or `pg_isready -h localhost -p 5432`
- Verify env vars: `echo $DB_HOST $DB_PORT $DB_NAME`

**"ModuleNotFoundError":**
- Run `./py/run.sh setup` to install dependencies
- Ensure you're using the correct Python: `which python3`

**"Address already in use":**
- Another process is using the port. Find it: `lsof -i :8080`
- Kill it: `kill -9 <PID>`, or change the port: `PORT=9080 ./py/run.sh dashboard`

**Dashboard shows no signals after CSV upload:**
- Ensure PostgreSQL has the schema applied (`./py/run.sh init-db`)
- Check the Flask terminal for error messages
- Verify CSV format matches the expected 31-column layout
