# Debug

Systematic debugging workflow for PreTradeAgents.

## Step 1: Reproduce

- Identify the failing module (market_analyst, trade_dashboard, trade_executor, learning_summary, shared)
- Run `python -m pytest tests/ -v` to see if tests catch the issue
- Check application logs for stack traces

## Step 2: Isolate

- Is it a data issue? Check the SQLAlchemy model and SQL migration
- Is it an NSE API issue? Check `shared/nse_client.py` retry logic and session warmup
- Is it a timezone issue? Verify `shared/time_utils.py` usage (must be IST)
- Is it a config issue? Check environment variables
- Is it an import issue? Run `./scripts/build.sh` to verify all module imports

## Step 3: Common Failure Patterns

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| `TypeError` / `None` on API data | NSE returned empty/null | Check null guards in collectors |
| `psycopg2.Error` column not found | Model doesn't match schema | Check SQL migration vs SQLAlchemy model |
| `ConnectionError` to NSE | NSE rate limiting | Verify session warmup in `nse_client.py`, add backoff |
| `ValueError` on timezone | Wrong timezone | Use `time_utils.now_ist()`, not `datetime.now()` |
| `ImportError` on shared module | Not running from project root | Run from `PreTradeAgents/` directory |
| Lot size returns -1 | Symbol not in registry | Add to `shared/lot_sizes.py` |

## Step 4: Fix and Verify

1. Write a failing test that reproduces the bug
2. Apply the fix
3. Run the test — must pass
4. Run full test suite: `python -m pytest tests/ -v`
5. If fix touches `shared/`, verify all agent imports: `./scripts/build.sh`
