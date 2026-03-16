# Doc Update

**This skill MUST be followed after every code change, before committing.**

## Mandatory Updates

After making any code change, update these files in the same commit:

### 1. CHANGELOG.md

Add an entry under `[Unreleased]` with the appropriate category:
- `Added` — new features, files, endpoints, models
- `Changed` — modifications to existing behavior
- `Fixed` — bug fixes
- `Removed` — deleted features, deprecated code

Format:
```markdown
### Added
- Short description of what was added
```

### 2. COMMIT_LOG.md

Add an entry at the top of the current date section:
```markdown
### Commit: <short description>

**Files changed:**
- `path/to/file.py` - Description of change

**Functional impact:**
- What behavior changed, was added, or was removed

**Breaking changes:**
- List any breaking changes, or "None"
```

### 3. docs/architecture.md (if applicable)

Update if you changed:
- Module dependencies
- Data flow between agents
- Database schema (new tables/columns)
- API integrations
- Port assignments

### 4. docs/decisions/ (if applicable)

Create a new ADR (`docs/decisions/NNN-title.md`) if you made a significant decision:
- Chose a new library or framework
- Changed the architecture pattern
- Added a new integration
- Changed deployment strategy

## Checklist Before Commit

- [ ] CHANGELOG.md updated
- [ ] COMMIT_LOG.md updated
- [ ] docs/architecture.md updated (if design changed)
- [ ] New ADR created (if significant decision made)
- [ ] Tests pass: `python -m pytest tests/ -v`
