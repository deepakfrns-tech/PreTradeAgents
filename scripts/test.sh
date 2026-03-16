#!/usr/bin/env bash
set -euo pipefail

# Run PreTrade Python tests using pytest.
#
# Usage:
#   ./scripts/test.sh                              # Run all tests
#   ./scripts/test.sh tests/test_formatters.py      # Run specific test file

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$ROOT_DIR"

if [[ $# -ge 1 ]]; then
    echo "=== Running tests: $* ==="
    python -m pytest "$@" -v
else
    echo "=== Running all tests ==="
    python -m pytest tests/ -v
fi
