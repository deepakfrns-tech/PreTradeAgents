#!/usr/bin/env bash
set -euo pipefail

# Deploy PreTradeAgents locally using Docker Compose
# Usage: ./scripts/local-deploy.sh [up|down|restart|logs|status]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
ACTION="${1:-up}"

cd "$ROOT_DIR"

# Check for .env file
if [[ ! -f .env ]] && [[ "$ACTION" == "up" || "$ACTION" == "restart" ]]; then
    echo "Warning: No .env file found."
    echo "Creating from .env.example — edit .env with your ANTHROPIC_API_KEY."
    cp .env.example .env
    echo ""
fi

case "$ACTION" in
    up)
        echo "=== Starting PreTradeAgents ==="
        docker compose up -d --build
        echo ""
        echo "Services:"
        echo "  PostgreSQL:       localhost:5432"
        echo "  Market Analyst:   http://localhost:8081"
        echo "  Trade Executor:   http://localhost:8082"
        echo "  Learning Summary: http://localhost:8083"
        echo ""
        echo "View logs: ./scripts/local-deploy.sh logs"
        echo "Stop:      ./scripts/local-deploy.sh down"
        ;;
    down)
        echo "=== Stopping PreTradeAgents ==="
        docker compose down
        echo "Stopped. Data is preserved in Docker volume."
        echo "To delete data: docker compose down -v"
        ;;
    restart)
        echo "=== Restarting PreTradeAgents ==="
        docker compose down
        docker compose up -d --build
        echo "Restarted."
        ;;
    logs)
        docker compose logs -f
        ;;
    status)
        docker compose ps
        ;;
    *)
        echo "Usage: $0 [up|down|restart|logs|status]"
        echo ""
        echo "  up      - Build and start all services (default)"
        echo "  down    - Stop all services"
        echo "  restart - Rebuild and restart all services"
        echo "  logs    - Tail logs from all services"
        echo "  status  - Show running services"
        exit 1
        ;;
esac
