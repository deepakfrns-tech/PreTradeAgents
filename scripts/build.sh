#!/usr/bin/env bash
set -euo pipefail

# Build all modules in correct dependency order
# Usage: ./scripts/build.sh [--skip-tests]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
SKIP_TESTS=""

if [[ "${1:-}" == "--skip-tests" ]]; then
    SKIP_TESTS="-DskipTests"
fi

echo "=== Building PreTradeAgents ==="
echo ""

echo "[1/5] Building shared-db..."
cd "$ROOT_DIR/shared-db"
mvn clean install $SKIP_TESTS -q
echo "      Done."

echo "[2/5] Building shared-utils..."
cd "$ROOT_DIR/shared-utils"
mvn clean install $SKIP_TESTS -q
echo "      Done."

echo "[3/5] Building agent-market-analyst..."
cd "$ROOT_DIR/agent-market-analyst"
mvn clean package $SKIP_TESTS -q
echo "      Done."

echo "[4/5] Building agent-trade-executor..."
cd "$ROOT_DIR/agent-trade-executor"
mvn clean package $SKIP_TESTS -q
echo "      Done."

echo "[5/5] Building agent-learning-summary..."
cd "$ROOT_DIR/agent-learning-summary"
mvn clean package $SKIP_TESTS -q
echo "      Done."

echo ""
echo "=== Build complete ==="
echo ""
echo "JARs:"
echo "  agent-market-analyst/target/agent-market-analyst-1.0.0-SNAPSHOT.jar"
echo "  agent-trade-executor/target/agent-trade-executor-1.0.0-SNAPSHOT.jar"
echo "  agent-learning-summary/target/agent-learning-summary-1.0.0-SNAPSHOT.jar"
