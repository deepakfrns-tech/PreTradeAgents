# ADR-001: Multi-Agent Microservice Architecture

## Status
Accepted

## Context
We need a system that collects pre-market data, executes paper trades, and learns from outcomes. These are distinct phases with different timing, resource needs, and failure modes.

## Decision
Split into three independent Spring Boot agents that communicate exclusively through a shared PostgreSQL database:
- Agent 1: Market Analyst (pre-market, 9:00-9:15 IST)
- Agent 2: Trade Executor (market hours, 9:15-15:30 IST)
- Agent 3: Learning Summary (post-market, ~16:00 IST)

## Consequences
- **Good**: Agents can fail independently, scale separately, and be developed in isolation
- **Good**: No complex inter-service communication (REST/gRPC/messaging) needed
- **Good**: Database serves as a natural audit trail
- **Bad**: No real-time communication between agents (acceptable for this use case)
- **Bad**: Database becomes a single point of failure
