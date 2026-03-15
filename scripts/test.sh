#!/usr/bin/env bash
set -euo pipefail

# Run tests across all modules
# Usage: ./scripts/test.sh [module-name]
# Examples:
#   ./scripts/test.sh              # Test all modules
#   ./scripts/test.sh shared-utils # Test only shared-utils

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

MODULES=("shared-db" "shared-utils" "agent-market-analyst" "trade-dashboard" "agent-trade-executor" "agent-learning-summary")

# If a specific module is requested
if [[ -n "${1:-}" ]]; then
    MODULES=("$1")
fi

PASSED=0
FAILED=0
FAILED_MODULES=()

echo "=== Running Tests ==="
echo ""

for module in "${MODULES[@]}"; do
    echo "--- Testing $module ---"
    cd "$ROOT_DIR/$module"
    if mvn test -q 2>&1; then
        echo "    PASSED"
        ((PASSED++))
    else
        echo "    FAILED"
        ((FAILED++))
        FAILED_MODULES+=("$module")
    fi
    echo ""
done

echo "=== Test Summary ==="
echo "Passed: $PASSED"
echo "Failed: $FAILED"

if [[ $FAILED -gt 0 ]]; then
    echo "Failed modules: ${FAILED_MODULES[*]}"
    exit 1
fi

echo "All tests passed."
