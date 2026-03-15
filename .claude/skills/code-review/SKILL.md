# Code Review

Perform a thorough code review on the specified files or most recent changes.

## Checklist

### 1. Correctness
- Does the logic match the intended behavior?
- Are edge cases handled (null, empty, zero, negative)?
- Are boundary conditions correct (off-by-one, inclusive/exclusive)?

### 2. Architecture
- Does this follow the module dependency graph? (agents → shared-db + shared-utils only)
- Are database entities in `shared-db`, not in agent modules?
- Are DTOs defined as static inner classes in their parent service?
- No inter-agent dependencies — agents communicate only through the database

### 3. NSE/Trading Specifics
- Timestamps use `TimeUtils` (IST), never system default?
- Currency formatting uses `Formatters` (Indian numbering)?
- F&O symbols validated via `LotSizes`?
- NSE API calls include session warmup?

### 4. Database Safety
- New entities have corresponding Flyway migrations?
- Existing migrations are NOT modified?
- JSONB columns use `@Column(columnDefinition = "jsonb")`?
- Unique constraints defined where needed?

### 5. Configuration
- No hardcoded secrets or API keys?
- Config uses `@ConfigurationProperties` with proper prefix?
- Environment variables documented?

### 6. Testing
- Unit tests exist for new/changed code?
- Tests cover happy path AND error cases?
- Static utility methods tested independently?

## Output Format

For each issue found, report:
- **File:Line** — location
- **Severity** — CRITICAL / WARNING / SUGGESTION
- **Issue** — what's wrong
- **Fix** — recommended change
