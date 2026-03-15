# shared-utils — Local Context

## What This Module Does
Cross-agent utility classes for NSE API access, IST time handling, Indian currency formatting, and F&O lot size validation.

## Key Classes

| Class | Critical Rule |
|-------|--------------|
| `NseClient` | Always warm up session before API calls. Max 3 retries with exponential backoff. |
| `TimeUtils` | ALL time operations must go through this class. Never use `LocalDateTime.now()` directly. |
| `Formatters` | Indian numbering system (lakhs/crores). `formatINR()` for currency, `formatPercent()` for %. |
| `LotSizes` | Registry of 50+ NSE F&O symbols. Returns -1 for invalid symbols. Case-insensitive. |

## Common Changes

### Adding a new F&O stock
Edit `LotSizes.java` → add `map.put("SYMBOL", lotSize)` in static block → update `LotSizesTest.java`.

### NSE API changes
Edit `NseClient.java` → update URL constants → test with caution (NSE rate limits aggressively).

## Build

```bash
mvn clean install   # Installs to local Maven repo for agents to consume
mvn test            # Run utility unit tests
```
