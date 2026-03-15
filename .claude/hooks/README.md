# Claude Code Hooks

Hooks are automated guardrails configured in `.claude/settings.json`.

## Active Hooks

### Pre-Commit: Doc Update Reminder
Triggers before every `git commit` to remind you to update documentation files.

**Configured in:** `.claude/settings.json` → `hooks.PreToolUse`

**What it checks:** Reminds you to update `CHANGELOG.md`, `COMMIT_LOG.md`, and relevant module `CLAUDE.md` files before committing.

## Recommended Additional Hooks

### Post-Edit: Compile Check
After any Java file edit, verify it compiles:
```bash
mvn -f <module>/pom.xml compile -q
```

### Post-Edit: Test on Core Changes
After changes to shared-db or shared-utils, run tests:
```bash
cd shared-db && mvn test -q && cd .. && cd shared-utils && mvn test -q
```

### Block: Protected Files
Never modify these:
- `shared-db/migrations/V001__*` through `V006__*` (applied migrations)
- `.env` (local secrets)
- `**/application-prod.yml` (production config)

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
            "command": "echo 'Check: CHANGELOG.md, COMMIT_LOG.md, module CLAUDE.md updated?'"
          }
        ]
      }
    ]
  }
}
```
