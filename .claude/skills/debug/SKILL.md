# Debug

Systematic debugging workflow for PreTradeAgents.

## Step 1: Reproduce

- Identify the failing module (agent-market-analyst, agent-trade-executor, agent-learning-summary, shared-db, shared-utils)
- Run `mvn test` in that module to see if tests catch the issue
- Check application logs for stack traces

## Step 2: Isolate

- Is it a data issue? Check the database entity and Flyway migration
- Is it an NSE API issue? Check `NseClient` retry logic and session warmup
- Is it a timezone issue? Verify `TimeUtils` usage (must be IST)
- Is it a config issue? Check `application.yml` and `*Settings.java`
- Is it a dependency issue? Check if `shared-db`/`shared-utils` are built and installed

## Step 3: Common Failure Patterns

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| `NullPointerException` on API data | NSE returned empty/null | Check null guards in collectors |
| `PSQLException` column not found | Entity doesn't match schema | Check Flyway migration vs JPA entity |
| `WebClientRequestException` | NSE rate limiting | Verify session warmup, add backoff |
| `DateTimeException` | Wrong timezone | Use `TimeUtils.nowIST()`, not `LocalDateTime.now()` |
| `ClassNotFoundException` shared model | shared-db not installed | Run `cd shared-db && mvn clean install` |
| Lot size returns -1 | Symbol not in registry | Add to `LotSizes.java` static block |

## Step 4: Fix and Verify

1. Write a failing test that reproduces the bug
2. Apply the fix
3. Run the test — must pass
4. Run full module tests: `mvn test`
5. If fix touches shared-db/shared-utils, rebuild and test dependent modules
