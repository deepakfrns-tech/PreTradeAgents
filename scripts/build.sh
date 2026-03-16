#!/usr/bin/env bash
set -euo pipefail

# Build PreTrade Python project — install dependencies and verify imports.
#
# Usage:
#   ./scripts/build.sh            # Install deps and verify
#   ./scripts/build.sh docker     # Build Docker images

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

case "${1:-install}" in
    install)
        echo "=== Installing Python dependencies ==="
        pip install -r "$ROOT_DIR/requirements.txt"
        echo ""
        echo "=== Verifying module imports ==="
        cd "$ROOT_DIR"
        python -c "from shared import models, database, time_utils, formatters, lot_sizes, nse_client; print('shared: OK')"
        python -c "from market_analyst import app, csv_export; print('market_analyst: OK')"
        python -c "from market_analyst.collectors import nse_collector, news_collector, technical_collector; print('collectors: OK')"
        python -c "from trade_dashboard import app, csv_parser; print('trade_dashboard: OK')"
        python -c "from trade_executor import app; print('trade_executor: OK')"
        python -c "from learning_summary import app; print('learning_summary: OK')"
        echo ""
        echo "=== Build complete ==="
        ;;
    docker)
        echo "=== Building Docker images ==="
        cd "$ROOT_DIR"
        docker compose build
        echo "=== Docker build complete ==="
        ;;
    *)
        echo "Usage: ./scripts/build.sh [install|docker]"
        exit 1
        ;;
esac
