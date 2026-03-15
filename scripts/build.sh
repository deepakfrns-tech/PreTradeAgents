#!/usr/bin/env bash
set -euo pipefail

# Build all modules in correct dependency order
# Usage: ./scripts/build.sh [--skip-tests] [module-name]
# Examples:
#   ./scripts/build.sh                    # Build everything
#   ./scripts/build.sh --skip-tests       # Build everything, skip tests
#   ./scripts/build.sh trade-dashboard    # Build only trade-dashboard (+ deps)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
SKIP_TESTS=""
TARGET_MODULE=""

for arg in "$@"; do
    case "$arg" in
        --skip-tests) SKIP_TESTS="-DskipTests" ;;
        *) TARGET_MODULE="$arg" ;;
    esac
done

build_module() {
    local name="$1"
    local dir="$2"
    local cmd="${3:-package}"
    echo "  Building $name..."
    cd "$ROOT_DIR/$dir"
    mvn clean $cmd $SKIP_TESTS -q
    echo "      Done."
}

echo "=== Building PreTradeAgents ==="
echo ""

# Always build shared libs first
build_module "shared-db" "shared-db" "install"
build_module "shared-utils" "shared-utils" "install"

if [[ -z "$TARGET_MODULE" ]] || [[ "$TARGET_MODULE" == "agent-market-analyst" ]]; then
    build_module "agent-market-analyst" "agent-market-analyst"
fi

if [[ -z "$TARGET_MODULE" ]] || [[ "$TARGET_MODULE" == "trade-dashboard" ]]; then
    build_module "trade-dashboard" "trade-dashboard"
fi

if [[ -z "$TARGET_MODULE" ]] || [[ "$TARGET_MODULE" == "agent-trade-executor" ]]; then
    build_module "agent-trade-executor" "agent-trade-executor"
fi

if [[ -z "$TARGET_MODULE" ]] || [[ "$TARGET_MODULE" == "agent-learning-summary" ]]; then
    build_module "agent-learning-summary" "agent-learning-summary"
fi

echo ""
echo "=== Build complete ==="
echo ""
echo "JARs:"
echo "  agent-market-analyst/target/agent-market-analyst-1.0.0-SNAPSHOT.jar     (port 8081)"
echo "  trade-dashboard/target/trade-dashboard-1.0.0-SNAPSHOT.jar               (port 8080)"
echo "  agent-trade-executor/target/agent-trade-executor-1.0.0-SNAPSHOT.jar     (port 8082)"
echo "  agent-learning-summary/target/agent-learning-summary-1.0.0-SNAPSHOT.jar (port 8083)"
echo ""
echo "Run individually:"
echo "  java -jar agent-market-analyst/target/agent-market-analyst-1.0.0-SNAPSHOT.jar"
echo "  java -jar trade-dashboard/target/trade-dashboard-1.0.0-SNAPSHOT.jar"
echo "  java -jar agent-trade-executor/target/agent-trade-executor-1.0.0-SNAPSHOT.jar"
echo "  java -jar agent-learning-summary/target/agent-learning-summary-1.0.0-SNAPSHOT.jar"
