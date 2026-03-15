# Refactor

Refactor code while preserving behavior. Follow these guidelines specific to PreTradeAgents.

## Pre-Refactor Checklist

1. Read the target file AND its tests before making changes
2. Run `mvn test` in the affected module to establish a green baseline
3. Check if the code is used by other modules (grep for imports across the project)

## Refactoring Rules

### Preserve
- All public method signatures unless explicitly asked to change them
- Lombok annotations (`@Data`, `@Builder`, etc.) — don't expand them manually
- Static inner DTO classes — don't extract to separate files unless asked
- `@Component` / `@Service` annotations — Spring needs them for DI

### Allowed Changes
- Extract private methods for readability
- Simplify conditional logic
- Remove dead code (unused imports, unreachable branches)
- Improve variable/method naming
- Add missing null checks at boundaries
- Replace magic numbers with named constants

### Forbidden Changes
- Don't change database column names (breaks Flyway)
- Don't rename public API endpoints
- Don't change package structure without updating all `@EntityScan` / `@EnableJpaRepositories`
- Don't modify Flyway migration files

## Post-Refactor Checklist

1. Run `mvn test` in the affected module — must be green
2. If shared-db or shared-utils changed, run tests in all dependent agents too
3. Verify no new compiler warnings
