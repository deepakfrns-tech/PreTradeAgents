# ADR-003: Independent Maven Modules (No Parent POM)

## Status
Accepted

## Context
The project has 5 Maven modules. A parent POM would centralize dependency management but add coupling.

## Decision
Each module is an independent Maven project with its own `pom.xml`. Shared libraries (`shared-db`, `shared-utils`) are installed to local Maven repo and consumed as dependencies.

## Consequences
- **Good**: Modules can use different Java versions (shared libs: Java 17, agents: Java 21)
- **Good**: Simpler CI/CD — each module builds independently
- **Good**: No version conflicts from parent POM overrides
- **Bad**: Must manually keep Spring Boot version in sync across modules
- **Bad**: Build order matters: shared-db → shared-utils → agents
