#!/usr/bin/env bash
set -euo pipefail

# Run individual agents or the full stack locally
# Prerequisites: PostgreSQL running, modules built (./scripts/build.sh)
#
# Usage:
#   ./scripts/run.sh postgres         # Start only PostgreSQL via Docker
#   ./scripts/run.sh analyst          # Run market analyst (port 8081)
#   ./scripts/run.sh dashboard        # Run trade dashboard (port 8080)
#   ./scripts/run.sh executor         # Run trade executor (port 8082)
#   ./scripts/run.sh learner          # Run learning summary (port 8083)
#   ./scripts/run.sh all              # Run all agents (use separate terminals)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

# Default environment variables for local PostgreSQL
export DB_HOST="${DB_HOST:-localhost}"
export DB_PORT="${DB_PORT:-5432}"
export DB_NAME="${DB_NAME:-pretrade}"
export DB_USERNAME="${DB_USERNAME:-pretrade}"
export DB_PASSWORD="${DB_PASSWORD:-pretrade}"

usage() {
    echo "Usage: $0 {postgres|analyst|dashboard|executor|learner|all}"
    echo ""
    echo "  postgres   - Start PostgreSQL via Docker (required first)"
    echo "  analyst    - Run Market Analyst agent (port 8081)"
    echo "  dashboard  - Run Trade Dashboard web app (port 8080)"
    echo "  executor   - Run Trade Executor agent (port 8082)"
    echo "  learner    - Run Learning Summary agent (port 8083)"
    echo "  all        - Show commands to run all services"
    exit 1
}

if [[ $# -lt 1 ]]; then
    usage
fi

case "$1" in
    postgres)
        echo "Starting PostgreSQL..."
        cd "$ROOT_DIR"
        docker compose up -d postgres
        echo "PostgreSQL running on port ${DB_PORT}"
        echo "Waiting for health check..."
        sleep 3
        docker compose exec postgres pg_isready -U pretrade -d pretrade
        echo "PostgreSQL is ready."
        ;;
    analyst)
        echo "Starting Market Analyst on port 8081..."
        java -jar "$ROOT_DIR/agent-market-analyst/target/agent-market-analyst-1.0.0-SNAPSHOT.jar"
        ;;
    dashboard)
        echo "Starting Trade Dashboard on port 8080..."
        java -jar "$ROOT_DIR/trade-dashboard/target/trade-dashboard-1.0.0-SNAPSHOT.jar"
        ;;
    executor)
        echo "Starting Trade Executor on port 8082..."
        java -jar "$ROOT_DIR/agent-trade-executor/target/agent-trade-executor-1.0.0-SNAPSHOT.jar"
        ;;
    learner)
        echo "Starting Learning Summary on port 8083..."
        java -jar "$ROOT_DIR/agent-learning-summary/target/agent-learning-summary-1.0.0-SNAPSHOT.jar"
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
        echo "Or run PostgreSQL + dashboard only:"
        echo "  ./scripts/run.sh postgres"
        echo "  ./scripts/run.sh dashboard"
        echo ""
        echo "Dashboard: http://localhost:8080"
        echo "Analyst API: http://localhost:8081/api/analyst/health"
        echo "Executor API: http://localhost:8082/api/executor/health"
        echo "Learner API: http://localhost:8083/api/learner/health"
        ;;
    *)
        usage
        ;;
esac
