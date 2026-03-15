#!/usr/bin/env bash
set -euo pipefail

# Run PreTrade Python agents independently.
#
# Usage:
#   ./py/run.sh setup             # Install Python dependencies
#   ./py/run.sh init-db           # Create DB tables (if not using Flyway)
#   ./py/run.sh postgres          # Start PostgreSQL via Docker
#   ./py/run.sh analyst           # Market Analyst (port 8081)
#   ./py/run.sh dashboard         # Trade Dashboard (port 8080)
#   ./py/run.sh executor          # Trade Executor (port 8082)
#   ./py/run.sh learner           # Learning Summary (port 8083)

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
        pip install -r "$SCRIPT_DIR/requirements.txt"
        echo "Done."
        ;;
    init-db)
        echo "Initializing database tables..."
        cd "$ROOT_DIR"
        for f in shared-db/migrations/V*.sql; do
            echo "  Applying $f..."
            PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USERNAME" -d "$DB_NAME" -f "$f" 2>/dev/null || true
        done
        echo "Done."
        ;;
    postgres)
        echo "Starting PostgreSQL..."
        cd "$ROOT_DIR"
        docker compose up -d postgres
        echo "PostgreSQL running on port $DB_PORT"
        sleep 2
        echo "Applying migrations..."
        for f in shared-db/migrations/V*.sql; do
            echo "  $f"
            PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USERNAME" -d "$DB_NAME" -f "$f" 2>/dev/null || true
        done
        echo "Database ready."
        ;;
    analyst)
        echo "Starting Market Analyst on port 8081..."
        cd "$SCRIPT_DIR"
        python -m market_analyst.app
        ;;
    dashboard)
        echo "Starting Trade Dashboard on port 8080..."
        cd "$SCRIPT_DIR"
        python -m trade_dashboard.app
        ;;
    executor)
        echo "Starting Trade Executor on port 8082..."
        cd "$SCRIPT_DIR"
        python -m trade_executor.app
        ;;
    learner)
        echo "Starting Learning Summary on port 8083..."
        cd "$SCRIPT_DIR"
        python -m learning_summary.app
        ;;
    help|*)
        echo "PreTrade Python Runner"
        echo ""
        echo "Usage: ./py/run.sh <command>"
        echo ""
        echo "Commands:"
        echo "  setup      Install Python dependencies (pip install)"
        echo "  postgres   Start PostgreSQL + apply migrations"
        echo "  init-db    Apply SQL migrations to existing PostgreSQL"
        echo "  analyst    Run Market Analyst (port 8081)"
        echo "  dashboard  Run Trade Dashboard (port 8080)"
        echo "  executor   Run Trade Executor (port 8082)"
        echo "  learner    Run Learning Summary (port 8083)"
        echo ""
        echo "Workflow:"
        echo "  1. ./py/run.sh setup              # one-time"
        echo "  2. ./py/run.sh postgres            # start DB"
        echo "  3. ./py/run.sh dashboard           # http://localhost:8080"
        echo "  4. Upload CSV in browser, approve trades"
        echo "  5. ./py/run.sh executor            # triggers at 9:15 AM"
        echo "  6. ./py/run.sh learner             # post-market analysis"
        ;;
esac
