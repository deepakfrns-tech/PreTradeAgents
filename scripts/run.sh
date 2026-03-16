#!/usr/bin/env bash
set -euo pipefail

# Run PreTrade Python agents independently.
#
# Usage:
#   ./scripts/run.sh setup             # Install Python dependencies
#   ./scripts/run.sh init-db           # Apply DB migrations
#   ./scripts/run.sh postgres          # Start PostgreSQL via Docker
#   ./scripts/run.sh analyst           # Market Analyst (port 8081)
#   ./scripts/run.sh dashboard         # Trade Dashboard (port 8080)
#   ./scripts/run.sh executor          # Trade Executor (port 8082)
#   ./scripts/run.sh learner           # Learning Summary (port 8083)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
CMD="${1:-help}"

export DB_HOST="${DB_HOST:-localhost}"
export DB_PORT="${DB_PORT:-5432}"
export DB_NAME="${DB_NAME:-pretrade}"
export DB_USERNAME="${DB_USERNAME:-pretrade}"
export DB_PASSWORD="${DB_PASSWORD:-pretrade}"

case "$CMD" in
    setup)
        echo "Installing Python dependencies..."
        pip install -r "$ROOT_DIR/requirements.txt"
        echo "Done."
        ;;
    init-db)
        echo "Applying database migrations..."
        for f in "$ROOT_DIR"/shared-db/migrations/V*.sql; do
            echo "  Applying $(basename "$f")..."
            PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USERNAME" -d "$DB_NAME" -f "$f" 2>/dev/null || true
        done
        echo "Done."
        ;;
    postgres)
        echo "Starting PostgreSQL..."
        cd "$ROOT_DIR"
        docker compose up -d postgres
        echo "PostgreSQL running on port $DB_PORT"
        echo "Waiting for health check..."
        sleep 3
        docker compose exec postgres pg_isready -U pretrade -d pretrade
        echo "Applying migrations..."
        for f in shared-db/migrations/V*.sql; do
            echo "  $(basename "$f")"
            PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USERNAME" -d "$DB_NAME" -f "$f" 2>/dev/null || true
        done
        echo "Database ready."
        ;;
    analyst)
        echo "Starting Market Analyst on port 8081..."
        cd "$ROOT_DIR"
        python -m market_analyst.app
        ;;
    dashboard)
        echo "Starting Trade Dashboard on port 8080..."
        cd "$ROOT_DIR"
        python -m trade_dashboard.app
        ;;
    executor)
        echo "Starting Trade Executor on port 8082..."
        cd "$ROOT_DIR"
        python -m trade_executor.app
        ;;
    learner)
        echo "Starting Learning Summary on port 8083..."
        cd "$ROOT_DIR"
        python -m learning_summary.app
        ;;
    all)
        echo "=== Run each service in a separate terminal ==="
        echo ""
        echo "Terminal 1: ./scripts/run.sh postgres"
        echo "Terminal 2: ./scripts/run.sh analyst"
        echo "Terminal 3: ./scripts/run.sh dashboard"
        echo "Terminal 4: ./scripts/run.sh executor"
        echo "Terminal 5: ./scripts/run.sh learner"
        echo ""
        echo "Dashboard: http://localhost:8080"
        echo "Analyst API: http://localhost:8081/api/analyst/health"
        echo "Executor API: http://localhost:8082/api/executor/health"
        echo "Learner API: http://localhost:8083/api/learner/health"
        ;;
    help|*)
        echo "PreTrade Python Runner"
        echo ""
        echo "Usage: ./scripts/run.sh <command>"
        echo ""
        echo "Commands:"
        echo "  setup      Install Python dependencies (pip install)"
        echo "  postgres   Start PostgreSQL + apply migrations"
        echo "  init-db    Apply SQL migrations to existing PostgreSQL"
        echo "  analyst    Run Market Analyst (port 8081)"
        echo "  dashboard  Run Trade Dashboard (port 8080)"
        echo "  executor   Run Trade Executor (port 8082)"
        echo "  learner    Run Learning Summary (port 8083)"
        echo "  all        Show commands to run all services"
        echo ""
        echo "Workflow:"
        echo "  1. ./scripts/run.sh setup        # one-time"
        echo "  2. ./scripts/run.sh postgres      # start DB"
        echo "  3. ./scripts/run.sh dashboard     # http://localhost:8080"
        echo "  4. Upload CSV in browser, approve trades"
        echo "  5. ./scripts/run.sh executor      # triggers at 9:15 AM"
        echo "  6. ./scripts/run.sh learner       # post-market analysis"
        ;;
esac
