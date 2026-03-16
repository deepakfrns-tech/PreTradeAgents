# Refactor

Refactor code while preserving behavior. Follow these guidelines specific to PreTradeAgents.

## Pre-Refactor Checklist

1. Read the target file AND its tests before making changes
2. Run `python -m pytest tests/ -v` to establish a green baseline
3. Check if the code is used by other modules (grep for imports across the project)

## Refactoring Rules

### Preserve
- All public function/method signatures unless explicitly asked to change them
- SQLAlchemy model column names — they map to database columns
- Flask route decorators and URL patterns
- Import paths used by other modules

### Allowed Changes
- Extract helper functions for readability
- Simplify conditional logic
- Remove dead code (unused imports, unreachable branches)
- Improve variable/function naming
- Add missing None/boundary checks
- Replace magic numbers with named constants

### Forbidden Changes
- Don't change database column names (breaks existing data)
- Don't rename public API endpoints
- Don't modify SQL migration files
- Don't change the `shared/` module's public API without updating all importers

## Post-Refactor Checklist

1. Run `python -m pytest tests/ -v` — must be green
2. If `shared/` changed, verify all agent imports still work: `./scripts/build.sh`
3. Verify no new warnings in test output
