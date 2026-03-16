# ADR-004: Trade Dashboard with CSV-based Workflow

## Status
Accepted

## Context
The original architecture had agents communicating solely through the database with no user interface for reviewing and approving trade signals before execution. This made it difficult to:
1. Review proposed trades with full context before market open
2. Manually approve/reject individual trades
3. Operate agents independently of each other

## Decision
Add a **Trade Dashboard** web application (Flask + Jinja2) that:
1. Accepts CSV files exported by the Market Analyst as the input mechanism
2. Displays trade signals in a scored dashboard with all analysis details
3. Allows users to select and approve trades, persisting them as TradeDecisions
4. Decouples the agents so each can run independently

The CSV file acts as a **portable handoff artifact** between Agent 1 and the dashboard, enabling:
- Offline review of signals
- Archival of daily signal sets
- Operation without requiring Agent 1 to be running when reviewing trades

## Consequences

### Positive
- User has full control over which trades get executed
- Each component can run independently (postgres → analyst → dashboard → executor → learner)
- CSV provides auditability and archival of daily signals
- No direct dependency between agents (communication remains DB-only)

### Negative
- Extra manual step (upload CSV) vs. automatic DB-to-DB flow
- Dashboard is a new module to maintain
- CSV format must stay in sync with StockAnalysis model fields

### Mitigations
- Dashboard also reads directly from DB if signals were written by the analyst
- CSV format is documented and has parser tests
- Dashboard handles both new imports and existing DB records
