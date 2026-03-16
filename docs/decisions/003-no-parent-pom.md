# ADR-003: Shared Library Pattern (No Framework Coupling)

## Status
Accepted (Updated: migrated from Java/Maven to Python)

## Context
The project has multiple agent modules that share common models and utilities. We need a way to share code without tight coupling.

## Decision
All shared code lives in a single `shared/` Python package imported by all agents. Each agent is an independent Flask application with its own entry point. SQL migrations are maintained separately in `shared-db/migrations/`.

## Consequences
- **Good**: Simple import — all agents just `from shared import models, time_utils, formatters`
- **Good**: No build order or package manager complexity
- **Good**: Each agent can be developed and tested independently
- **Bad**: Shared module changes affect all agents (mitigated by tests)
- **Bad**: All agents must run from project root for imports to resolve
