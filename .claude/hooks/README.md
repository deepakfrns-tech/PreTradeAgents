# Claude Code Hooks

Hooks are automated guardrails that run before or after Claude takes actions.
Configure them in `.claude/settings.json` or via Claude Code CLI.

## Recommended Hooks for PreTradeAgents

### Post-Edit: Format Check
After any Java file edit, verify formatting:
```bash
# Hook: after editing .java files
mvn -f <module>/pom.xml compile -q
```

### Post-Edit: Test on Core Changes
After changes to shared-db or shared-utils, run tests:
```bash
# Hook: after editing shared-db/**/*.java or shared-utils/**/*.java
cd shared-db && mvn test -q && cd .. && cd shared-utils && mvn test -q
```

### Block: Protected Directories
Prevent modifications to applied Flyway migrations:
```
# Block edits to: shared-db/migrations/V001__* through V006__*
# These migrations have been applied and must not change
```

### Block: Sensitive Files
Never edit or commit:
```
.env
**/application-prod.yml
**/credentials*
```

## How to Enable

Add hooks to `.claude/settings.json`:
```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "echo 'Remember: run mvn test after changes'"
          }
        ]
      }
    ]
  }
}
```

See [Claude Code docs](https://docs.anthropic.com/en/docs/claude-code) for full hook configuration.
