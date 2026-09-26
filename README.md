# ShopBook — Daily Expense Tracker

A database-free expense tracker for small shops and personal finance. Everything
is stored in a single Excel workbook (`.xlsx`) through Apache POI, so there is no
database to install or manage — the workbook *is* the database, and you can open
it directly in Excel at any time.

- **Frontend:** React 18 + Vite + Material UI, responsive with light/dark mode.
- **Backend:** Spring Boot 3 (Java 17) REST API.
- **Storage:** Apache POI reads/writes a workbook with a master `Buyers` sheet,
  a global `Transactions` sheet, and one worksheet per buyer.
- **Exports:** Balance sheet to Excel and PDF (OpenPDF).

## Project structure

```
daily-expense-tracker/
├── backend/                         Spring Boot API + Apache POI storage
│   ├── pom.xml
│   └── src/main/java/com/shoptracker/expense/
│       ├── ExpenseTrackerApplication.java
│       ├── config/                  WebConfig (CORS), ExcelProperties
│       ├── controller/              Dashboard, Transaction, Buyer, BalanceSheet,
│       │                            Report, Data (backup/import/export)
│       ├── service/
│       │   ├── excel/               ExcelStorageService, ExcelSchema  ← core engine
│       │   ├── BuyerService, TransactionService, DashboardService,
│       │   ├── BalanceSheetService, ReportService, PdfExportService
│       ├── model/                   Buyer, Transaction, enums
│       ├── dto/                     Request/response objects
│       ├── exception/               Custom exceptions + GlobalExceptionHandler
│       └── util/                    PoiUtils
│   └── data/                        Workbook + timestamped backups (created at runtime)
│
└── frontend/                        React + Vite + MUI client
    ├── package.json, vite.config.js, index.html
    └── src/
        ├── main.jsx, App.jsx        Shell: top bar, bottom nav, floating (+)
        ├── api/client.js            Axios layer for every endpoint
        ├── context/                 Color mode + toast providers
        ├── theme/theme.js           Teal/amber light + dark themes
        ├── components/              StatCard, Charts, TransactionTable,
        │                            TransactionForm, BuyerForm, AddSpeedDial
        ├── pages/                   Dashboard, Transactions, Buyers,
        │                            BalanceSheet, Reports
        ├── hooks/ + utils/
```

## Workbook layout

```
expense-tracker.xlsx
├── Dashboard        Auto-generated totals (regenerated on every save)
├── Buyers           Name | Mobile | Address | Opening Balance | Current Balance
├── Transactions     Id | Date | Buyer | Type | Amount | Description | Payment Mode
├── Rahim            Date | Credit | Debit | Balance | Notes   ← one tab per buyer
├── Kumar            Date | Credit | Debit | Balance | Notes
└── Senthil          Date | Credit | Debit | Balance | Notes
```

Creating a buyer adds a worksheet named after them. Every transaction linked to a
buyer is mirrored into that buyer's tab with a running balance, and balances are
recomputed automatically on each change.

## Prerequisites

- Java 17+ and Maven 3.9+
- Node.js 18+ and npm

## Run the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. On first run it creates
`backend/data/expense-tracker.xlsx`. A timestamped backup is written to
`backend/data/backups/` before every write (toggle with `app.excel.auto-backup`).

## Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000`. The dev server proxies `/api` to the backend, so no
extra configuration is needed. To point at a different backend, copy
`.env.example` to `.env` and set `VITE_API_BASE`.

## API reference

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/dashboard` | All dashboard metrics in one call |
| GET | `/api/transactions` | List all transactions |
| GET | `/api/transactions/search?q=&from=&to=&buyer=&type=` | Search + filter |
| POST | `/api/transactions` | Add transaction |
| PUT | `/api/transactions/{id}` | Edit transaction |
| DELETE | `/api/transactions/{id}` | Delete transaction |
| GET | `/api/buyers` | List buyers |
| GET | `/api/buyers/search?q=` | Instant buyer search |
| POST | `/api/buyers` | Create buyer (rejects duplicate names) |
| GET | `/api/balance-sheet` | Real-time balance sheet |
| GET | `/api/balance-sheet/export/excel` | Download workbook |
| GET | `/api/balance-sheet/export/pdf` | Download balance sheet PDF |
| GET | `/api/reports/{daily,weekly,monthly}` | Period reports |
| GET | `/api/reports/buyer/{name}` | Buyer-wise report |
| GET | `/api/reports/outstanding` | Outstanding balances |
| POST | `/api/data/backup` | Force a backup |
| POST | `/api/data/import` | Import an existing workbook (multipart) |
| GET | `/api/data/export` | Download the current workbook |

All JSON responses use the envelope `{ success, message, data }`.

## Notes

- Data validation runs both on the client (forms) and server (Bean Validation).
- File operations are wrapped so any IO/POI failure returns a clean error.
- Import replaces the current dataset after taking a backup.
