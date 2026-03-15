# ADR-002: Claude AI for Analysis and Learning

## Status
Accepted

## Context
Stock analysis requires pattern recognition across multiple data dimensions (gaps, sentiment, volume, options flow). Rule-based systems are brittle and hard to maintain.

## Decision
Use Anthropic Claude API for AI-powered analysis:
- **Agents 1 & 2**: Claude Sonnet (4k tokens) for real-time analysis and trade validation
- **Agent 3**: Claude Opus (8k tokens) for deeper pattern mining and strategy extraction

## Consequences
- **Good**: Flexible, nuanced analysis that adapts to market conditions
- **Good**: Natural language reasoning provides explainable decisions
- **Bad**: External API dependency (latency, rate limits, cost)
- **Bad**: API key management required
- **Mitigation**: Graceful degradation — agents continue with available data if Claude is unavailable
