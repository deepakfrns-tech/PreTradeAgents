# Code Review

Perform a thorough code review on the specified files or most recent changes.

## Checklist

### 1. Correctness
- Does the logic match the intended behavior?
- Are edge cases handled (None, empty, zero, negative)?
- Are boundary conditions correct (off-by-one, inclusive/exclusive)?

### 2. Architecture
- Does this follow the module dependency graph? (agents → shared only)
- Are database models in `shared/models.py`, not in agent modules?
- No inter-agent dependencies — agents communicate only through the database

### 3. NSE/Trading Specifics
- Timestamps use `shared.time_utils` (IST), never system default?
- Currency formatting uses `shared.formatters` (Indian numbering)?
- F&O symbols validated via `shared.lot_sizes`?
- NSE API calls include session warmup?

### 4. Database Safety
- New tables have corresponding SQL migrations in `shared-db/migrations/`?
- Existing migrations are NOT modified?
- JSONB columns properly handled in SQLAlchemy model?
- Unique constraints defined where needed?

### 5. Configuration
- No hardcoded secrets or API keys?
- Config uses environment variables via `os.environ`?
- Environment variables documented?

### 6. Testing
- Unit tests exist for new/changed code?
- Tests cover happy path AND error cases?
- Utility functions tested independently?

## Output Format

For each issue found, report:
- **File:Line** — location
- **Severity** — CRITICAL / WARNING / SUGGESTION
- **Issue** — what's wrong
- **Fix** — recommended change
