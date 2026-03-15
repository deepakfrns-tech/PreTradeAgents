# trade-dashboard — Local Context

## What This Module Does
Web dashboard (port 8080) for the PreTrade system. Upload CSV files from Market Analyst, view trade signals in a scoring dashboard, and select trades for execution. Approved trades are persisted as TradeDecisions in the database for the Trade Executor to pick up.

## Data Flow

```
CSV File (from Market Analyst) → Upload → StockAnalysis (DB)
                                         ↓
                               Dashboard (view signals)
                                         ↓
                               Select + Approve → TradeDecision (DB)
```

## Key Files

| File | Purpose |
|------|---------|
| `controller/DashboardController.java` | Upload, dashboard view, trade approval |
| `service/CsvParserService.java` | Parses CSV with quoted field support |
| `db/StockAnalysisRepository.java` | Signal queries by date |
| `db/TradeDecisionRepository.java` | Decision persistence |
| `templates/dashboard.html` | Signal table + detail cards (Thymeleaf) |
| `templates/upload.html` | CSV upload with drag-and-drop |
| `static/css/dashboard.css` | Dark-themed responsive styling |

## Key Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/` | Dashboard (today's date) |
| GET | `/dashboard?date=YYYY-MM-DD` | Dashboard for specific date |
| GET | `/upload` | Upload page |
| POST | `/upload` | Process CSV upload |
| POST | `/approve-trades` | Approve selected signals |

## Build & Test

```bash
# Requires shared-db and shared-utils installed first
mvn clean package
mvn test
```
