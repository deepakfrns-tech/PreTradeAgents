# Release

Steps to prepare and execute a release of PreTradeAgents.

## Pre-Release Checklist

1. All tests pass across all modules:
   ```bash
   cd shared-db && mvn test && cd ..
   cd shared-utils && mvn test && cd ..
   cd agent-market-analyst && mvn test && cd ..
   cd agent-trade-executor && mvn test && cd ..
   cd agent-learning-summary && mvn test && cd ..
   ```

2. CHANGELOG.md updated with all changes under the new version header
3. COMMIT_LOG.md updated with recent commits and functional impact
4. No uncommitted changes: `git status` is clean
5. No hardcoded secrets or debug code left in source

## Release Steps

1. Update version in all `pom.xml` files (5 modules)
2. Update CHANGELOG.md: move `[Unreleased]` items under new version header
3. Build all modules:
   ```bash
   cd shared-db && mvn clean install
   cd shared-utils && mvn clean install
   cd agent-market-analyst && mvn clean package
   cd agent-trade-executor && mvn clean package
   cd agent-learning-summary && mvn clean package
   ```
4. Commit with message: `release: vX.Y.Z`
5. Tag: `git tag vX.Y.Z`

## Post-Release

1. Add new `[Unreleased]` section to CHANGELOG.md
2. Verify all three agent JARs are built in `target/` directories
3. Document any new environment variables or configuration changes
