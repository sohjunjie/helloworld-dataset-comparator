# Data Comparator — Design Decisions

> All 35 decisions settled across 5 grilling rounds. This document is the single source of truth for the application's design.

---

## 1. Project Structure & Architecture

| # | Decision | Answer |
|---|----------|--------|
| Q1 | Repo structure | **Monorepo** — `frontend/` and `backend/` directories |
| Q3 | Authentication | **None for v1** — open tool, session-based isolation via server-generated comparison ID |
| Q8 | Backend build tool | **Maven** |
| Q28 | Dev ports | Angular on `4200`, Spring Boot on `8080`, `proxy.conf.json` forwarding `/api/**` |
| Q34 | Naming | `groupId=com.comparator`, `artifactId=data-comparator-backend`, Angular = `data-comparator-frontend` |

## 2. Data Input

| # | Decision | Answer |
|---|----------|--------|
| Q2 | Database support | **PostgreSQL only**, user provides connection details (host, port, db, user, password), **SELECT-only** |
| Q7 | File size limit | **500 MB** default, configurable in `application.yml` |
| Q10 | Column selection UX | **Auto-detect headers** into multi-select dropdown + **manual text override** fallback |
| Q16 | SQL editor | **CodeMirror 6** with SQL syntax highlighting |
| Q30 | Upload UX | **Drag-and-drop zone + file picker** button |
| Q31 | Delimiter detection | **Auto-detect** among comma, tab, pipe, semicolon; plus **custom delimiter** text input that overrides auto-detection |

## 3. Backend Engine

| # | Decision | Answer |
|---|----------|--------|
| Q4 | Parquet engine | **DuckDB embedded** — read/write Parquet, SQL-based comparison |
| Q5 | Progress updates | **SSE (Server-Sent Events)** — uploading → converting → comparing → done |
| Q13 | File cleanup | **TTL-based**, **1 hour** default, configurable in `application.yml` |
| Q17 | Parquet storage | Configurable path in `application.yml`, default `./data` |
| Q20 | Concurrent comparisons | **Multiple** — each comparison gets its own ID, independent results |
| Q21 | Excel generation | **Apache POI SXSSF** — streaming writes for large datasets |
| Q35 | Comparison timeout | **Configurable**, 30 min default in `application.yml` |
| Q36 | History storage | **Embedded H2** database (file-based), Spring Boot JPA, `comparisons` table |

## 4. Comparison Logic

| # | Decision | Answer |
|---|----------|--------|
| Q6 | Duplicate keys | **One-to-many** cross-comparison |
| Q18 | Match semantics | **"Not matching"** = total records without exact counterpart (superset); **"Missing"** = key not found at all (subset) |
| Q23 | Column mismatch | **Treat missing columns as NULL** — compare all columns, add NULLs where a column doesn't exist |
| Q24 | Case sensitivity | **User-configurable** toggle on config page, default **case-sensitive** |
| Q25 | Non-numeric tolerance | **Skip** — apply tolerance only when both values are numeric, **exact-match** fallback otherwise |

## 5. Frontend UI

| # | Decision | Answer |
|---|----------|--------|
| Q9 | UI component library | **Angular Material** |
| Q15 | Workflow layout | **Two-page flow** — `/compare` (configuration) → `/results/:comparisonId` (results) |
| Q19 | Dashboard visuals | **Metric cards + charts** (bar/donut) |
| Q22 | Charting library | **Chart.js + ng2-charts** |
| Q26 | Mismatch display | **Side-by-side table** — DS1 left, DS2 right, differing cells highlighted in orange/red |
| Q27 | History page | **Yes** — listing recent comparisons within TTL window |
| Q14 | Mismatch highlighting | **Yes** — highlight specific differing columns |
| Q32 | Error handling | **Inline** for field-level errors, **snackbar toasts** for backend errors |
| Q33 | Dark mode | **Light only** for v1 |

## 6. Reports

| # | Decision | Answer |
|---|----------|--------|
| Q11 | Dashboard pagination | **Server-side** via DuckDB `LIMIT/OFFSET` |
| Q12 | Excel report structure | **Summary sheet** (overview) + **detail sheets** (one per category) |
| Q29 | Excel row limit | **Split across multiple sheets** if >1M rows |

---

## Tech Stack Summary

```
┌─────────────────────────────────────────────────┐
│                   FRONTEND                       │
│  Angular 22 + Angular Material                   │
│  CodeMirror 6 (SQL editor)                       │
│  Chart.js + ng2-charts (dashboard charts)        │
│  Port: 4200                                      │
├─────────────────────────────────────────────────┤
│                proxy.conf.json                   │
│              /api/** → :8080                     │
├─────────────────────────────────────────────────┤
│                   BACKEND                        │
│  Spring Boot (Java 21) + Maven                   │
│  DuckDB (embedded, Parquet R/W + comparison)     │
│  H2 (embedded, comparison history/metadata)      │
│  Apache POI SXSSF (Excel reports)                │
│  PostgreSQL JDBC (SQL dataset input)             │
│  SSE (progress events)                           │
│  Port: 8080                                      │
├─────────────────────────────────────────────────┤
│                   STORAGE                        │
│  Parquet files on disk (configurable path)       │
│  TTL-based cleanup (1h default)                  │
└─────────────────────────────────────────────────┘
```

## Application Routes

| Route | Purpose |
|-------|---------|
| `/` or `/history` | Comparison history list |
| `/compare` | Configuration page — upload files, enter SQL, set keys/tolerances |
| `/results/:comparisonId` | Results dashboard — summary cards, charts, detail tables |

## Key `application.yml` Properties

```yaml
app:
  storage:
    path: ./data
  upload:
    max-file-size: 500MB
  cleanup:
    ttl-hours: 1
  comparison:
    timeout-minutes: 30
```
