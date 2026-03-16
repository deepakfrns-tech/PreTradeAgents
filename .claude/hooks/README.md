# Claude Code Hooks

Hooks are automated guardrails configured in `.claude/settings.json`.

## Active Hooks

### Pre-Commit: Doc Update Reminder
Triggers before every `git commit` to remind you to update documentation files.

**Configured in:** `.claude/settings.json` → `hooks.PreToolUse`

**What it checks:** Reminds you to update `CHANGELOG.md`, `COMMIT_LOG.md`, and relevant docs before committing.

## Recommended Additional Hooks

### Post-Edit: Import Check
After any Python file edit, verify imports still work:
```bash
python -c "from shared import models, time_utils, formatters, lot_sizes"
```

### Post-Edit: Test on Core Changes
After changes to shared/, run tests:
```bash
python -m pytest tests/ -v
```

### Block: Protected Files
Never modify these:
- `shared-db/migrations/V001__*` through `V007__*` (applied migrations)
- `.env` (local secrets)

## How to Add Hooks

Edit `.claude/settings.json` and add entries under the `hooks` key:

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash(git commit*)",
        "hooks": [
          {
            "type": "command",
            "command": "echo 'Check: CHANGELOG.md, COMMIT_LOG.md updated?'"
          }
        ]
      }
    ]
  }
}
```
