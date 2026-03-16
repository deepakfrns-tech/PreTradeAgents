# Release

Steps to prepare and execute a release of PreTradeAgents.

## Pre-Release Checklist

1. All tests pass:
   ```bash
   python -m pytest tests/ -v
   ```

2. CHANGELOG.md updated with all changes under the new version header
3. COMMIT_LOG.md updated with recent commits and functional impact
4. No uncommitted changes: `git status` is clean
5. No hardcoded secrets or debug code left in source

## Release Steps

1. Update version in CHANGELOG.md: move `[Unreleased]` items under new version header
2. Run all tests:
   ```bash
   python -m pytest tests/ -v
   ```
3. Verify all module imports:
   ```bash
   ./scripts/build.sh
   ```
4. Commit with message: `release: vX.Y.Z`
5. Tag: `git tag vX.Y.Z`

## Post-Release

1. Add new `[Unreleased]` section to CHANGELOG.md
2. Verify Docker images build: `./scripts/build.sh docker`
3. Document any new environment variables or configuration changes
