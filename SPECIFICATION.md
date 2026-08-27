# Data Comparator — Specification

## Problem Statement

Data professionals regularly need to compare two datasets to identify differences — matching records, mismatched values, and missing entries. Today, this process is manual, error-prone, and impractical for large datasets. Users resort to ad-hoc Excel formulas, custom scripts, or loading entire datasets into memory, which fails at scale and lacks a standardized workflow for defining match keys, applying numeric tolerances, and producing auditable comparison reports.

## Solution

A web application that lets users upload or query two datasets, configure comparison keys and tolerance rules, and receive a visual dashboard and downloadable Excel report showing exactly which records match, differ, or are missing between the two datasets. The system handles large datasets efficiently by converting inputs to Parquet format on disk and performing comparisons via embedded DuckDB SQL, never loading entire datasets into memory.

## User Stories

1. As a data analyst, I want to upload an Excel file (.xls/.xlsx) as dataset 1, so that I can compare its contents against another dataset.
2. As a data analyst, I want to upload a CSV file as dataset 1 or dataset 2, so that I can compare comma-separated data.
3. As a data analyst, I want to upload a plain text file (.txt) as a dataset, so that I can compare delimited text data.
4. As a data analyst, I want to drag and drop a file onto the upload zone, so that I can quickly provide a dataset without navigating a file picker.
5. As a data analyst, I want to click a file picker button as a fallback to drag-and-drop, so that I can upload files on devices where drag-and-drop is inconvenient.
6. As a data analyst, I want the system to auto-detect the delimiter (comma, tab, pipe, or semicolon) of my CSV/TXT file, so that I don't have to manually specify it.
7. As a data analyst, I want to manually choose a delimiter when the auto-detection is wrong, so that I retain control over how my data is parsed.
8. As a data analyst, I want to enter a custom delimiter character, so that I can parse files with unconventional delimiters (e.g., tilde, caret).
9. As a data analyst, I want the system to default to comma when it cannot confidently auto-detect a delimiter, so that parsing always proceeds without blocking.
10. As a database user, I want to enter a SQL SELECT query in a syntax-highlighted editor to produce dataset 1 or dataset 2, so that I can compare live database content.
11. As a database user, I want to provide PostgreSQL connection details (host, port, database, username, password) in the UI, so that my SQL query can be executed against my database.
12. As a database user, I want the system to restrict SQL execution to SELECT statements only, so that my database is protected from accidental writes.
13. As a data analyst, I want the system to parse column headers from my uploaded file and present them as a selectable dropdown, so that I can easily pick match key columns.
14. As a data analyst, I want to manually type column names as a fallback, so that I can specify keys even when auto-detection fails or for SQL result sets.
15. As a data analyst, I want to specify one or more columns as match keys, so that records in dataset 1 are matched against records in dataset 2 by those key values.
16. As a data analyst, I want to define zero or more tolerance columns with a percentage between 0% and 100%, so that numeric comparisons allow slight differences within the specified range.
17. As a data analyst, I want tolerance to apply only when both compared values are numeric, so that non-numeric values in a tolerance column fall back to exact-match comparison.
18. As a data analyst, I want a case-sensitivity toggle on the configuration page, so that I can choose whether string comparisons are case-sensitive or not.
19. As a data analyst, I want to click a "Compare" button to confirm my settings and start the comparison, so that I have explicit control over when processing begins.
20. As a data analyst, I want to see real-time progress updates (uploading → converting → comparing → done) while the comparison runs, so that I know the system is working and how far along it is.
21. As a data analyst, I want to see a dashboard with summary metric cards showing: record count in dataset 1, record count in dataset 2, fully matching records (both directions), non-matching records (both directions), and missing records (both directions), so that I get an instant overview of the comparison result.
22. As a data analyst, I want to see a bar or donut chart alongside the summary metrics, so that I can visually grasp the match/mismatch/missing distribution.
23. As a data analyst, I want to see a side-by-side table of mismatched record pairs (dataset 1 on the left, dataset 2 on the right), so that I can inspect exactly which records differ.
24. As a data analyst, I want differing cell values in the side-by-side table highlighted in orange or red, so that I can immediately spot what changed.
25. As a data analyst, I want to see a table of records present in dataset 1 but missing from dataset 2, so that I can identify gaps.
26. As a data analyst, I want to see a table of records present in dataset 2 but missing from dataset 1, so that I can identify additions or gaps in the reverse direction.
27. As a data analyst, I want the detail record tables to use server-side pagination, so that the dashboard remains responsive even with millions of difference records.
28. As a data analyst, I want to download a nicely formatted Excel report of the comparison results, so that I can share findings with stakeholders offline.
29. As a data analyst, I want the Excel report to have a summary sheet and separate detail sheets for each category (matches, mismatches DS1→DS2, mismatches DS2→DS1, missing from DS2, missing from DS1), so that the report is well-organized.
30. As a data analyst, I want the Excel report to split detail sheets when row count exceeds Excel's ~1M row limit, so that no data is lost.
31. As a data analyst, I want to start multiple comparisons concurrently, each with its own ID, so that I can compare several dataset pairs in parallel.
32. As a data analyst, I want a history page listing my recent comparisons (within the TTL window), so that I can revisit past results without bookmarking URLs.
33. As a data analyst, I want the system to handle datasets with different column sets by treating missing columns as NULL, so that comparisons across structurally different datasets still work.
34. As a data analyst, I want the system to support one-to-many key matching (multiple records sharing the same key in one dataset), so that duplicate-key scenarios are handled correctly per the cross-comparison logic.
35. As a data analyst, I want field-level inline error messages for configuration mistakes (e.g., invalid delimiter), so that I know exactly what to fix.
36. As a data analyst, I want snackbar toast notifications for backend errors (e.g., SQL execution failed, connection refused), so that I'm informed of system-level issues without losing my form state.
37. As a data analyst, I want uploaded files limited to a configurable maximum size (default 500 MB), so that the server is protected from resource exhaustion.
38. As a data analyst, I want comparison history and Parquet files automatically cleaned up after a configurable TTL (default 1 hour), so that disk space is managed without manual intervention.
39. As a data analyst, I want long-running comparisons to time out after a configurable duration (default 30 minutes), so that runaway processes don't consume server resources indefinitely.

## Implementation Decisions

### Project Structure

- **Monorepo** with `frontend/` and `backend/` directories in a single repository.
- **Backend**: Spring Boot on Java 21, built with **Maven**. Maven artifact: `groupId=com.comparator`, `artifactId=data-comparator-backend`.
- **Frontend**: Angular 22 standalone component architecture. Project name: `data-comparator-frontend`.
- **No authentication** for v1. Session-based isolation using a server-generated comparison ID.

### Backend Architecture

- **Comparison engine**: Embedded **DuckDB** handles Parquet read/write and executes comparison logic as SQL JOINs on Parquet files. No full dataset loading into Java heap.
- **History/metadata storage**: Embedded **H2** database (file-based), managed via Spring Boot JPA. A `comparisons` table stores comparison ID, timestamp, status, configuration snapshot, and summary statistics.
- **Excel generation**: **Apache POI SXSSF** (streaming API) for low-memory Excel report creation. Summary sheet first, then one detail sheet per result category. Detail sheets split across multiple sheets if row count exceeds ~1M.
- **Progress signaling**: **Server-Sent Events (SSE)** push status updates (uploading → converting → comparing → done) to the frontend.
- **File storage**: Parquet files written to a **configurable disk path** (default: `C:\Users\Admin\Documents\node-projects\helloworld-data-comparator\data`), set in `application.yml`.
- **File cleanup**: A scheduled Spring task purges Parquet files and comparison data older than a **configurable TTL** (default: 1 hour).
- **Comparison timeout**: Configurable in `application.yml` (default: 30 minutes).
- **Upload size limit**: Configurable in `application.yml` (default: 500 MB).
- **Database connectivity**: PostgreSQL only. The user provides JDBC connection details per comparison. SQL execution restricted to SELECT statements only.

### Delimiter Detection

- Auto-detect among **comma, tab, pipe (`|`), semicolon** by analyzing the first few lines of the dataset.
- If auto-detection cannot determine the delimiter with confidence, **default to comma**.
- Users may override by selecting a known delimiter or typing a **custom delimiter character**.

### Comparison Semantics

- **Match key**: One or more user-specified columns. Records are matched across datasets by composite key.
- **One-to-many**: When multiple records share the same key within a dataset, each is cross-compared against all records with that key in the other dataset.
- **"Not matching"**: Total records in a dataset that have no fully identical counterpart in the other dataset. This is the **superset**.
- **"Missing"**: Records whose key combination does not exist at all in the other dataset. This is a **subset** of "not matching."
- **Tolerance**: Applied only when both compared values parse as numeric. For non-numeric values in a tolerance column, exact-match comparison applies.
- **Case sensitivity**: User-configurable toggle, default case-sensitive.
- **Column mismatch**: When datasets have different column sets, missing columns are treated as NULL. A non-NULL value vs NULL is a mismatch.

### Frontend Architecture

- **UI framework**: Angular Material.
- **SQL editor**: CodeMirror 6 with SQL syntax highlighting.
- **Charts**: Chart.js + ng2-charts for summary dashboard visualizations.
- **Theming**: Light mode only for v1.
- **Error handling**: Inline validation messages for field-level errors; Angular Material `MatSnackBar` toasts for backend/system errors.

### Application Routes

| Route | Page | Purpose |
|-------|------|---------|
| `/` or `/history` | History | List of recent comparisons within TTL window |
| `/compare` | Configuration | Upload files or enter SQL, configure keys, tolerances, delimiter, case-sensitivity |
| `/results/:comparisonId` | Results Dashboard | Summary cards, charts, paginated detail tables, Excel download |

### REST API Surface (indicative)

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `POST` | `/api/v1/comparisons` | Create a new comparison (upload files + config) |
| `GET` | `/api/v1/comparisons` | List comparison history |
| `GET` | `/api/v1/comparisons/{id}` | Get comparison status and summary |
| `GET` | `/api/v1/comparisons/{id}/events` | SSE stream for progress updates |
| `GET` | `/api/v1/comparisons/{id}/results/mismatches` | Paginated mismatch detail records |
| `GET` | `/api/v1/comparisons/{id}/results/missing` | Paginated missing detail records |
| `GET` | `/api/v1/comparisons/{id}/results/matches` | Paginated matching records |
| `GET` | `/api/v1/comparisons/{id}/report` | Download Excel report |
| `POST` | `/api/v1/comparisons/{id}/headers` | Parse and return detected column headers |

### Dev Environment

- Angular dev server on port **4200**.
- Spring Boot on port **8080**.
- `proxy.conf.json` forwards `/api/**` requests to `localhost:8080`.

### Key `application.yml` Properties

```yaml
app:
  storage:
    path: C:\Users\Admin\Documents\node-projects\helloworld-data-comparator\data
  upload:
    max-file-size: 500MB
  cleanup:
    ttl-hours: 1
  comparison:
    timeout-minutes: 30

spring:
  datasource:
    url: jdbc:h2:file:${app.storage.path}/comparator-db
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
```

## Testing Decisions

### What Makes a Good Test

- Tests should exercise **external behavior through the highest available seam**, not internal implementation details.
- A test should describe a user-visible scenario: "when I upload two CSV files and compare by column X, the summary shows N mismatches."
- Tests should be resilient to internal refactoring — changing service internals should not break tests as long as the API contract holds.

### Testing Seams

Two primary seams cover the entire application:

1. **Backend — REST API boundary** (`@SpringBootTest` + `MockMvc`): The single highest seam for the backend. Every test exercises the full stack from HTTP request through controller → service → DuckDB/Parquet → response. Test fixtures are small Parquet/CSV/Excel files and SQL statements. This seam validates:
   - File upload and parsing (all formats: CSV, TXT, XLS, XLSX)
   - Delimiter auto-detection and custom delimiter handling
   - Parquet conversion pipeline
   - Comparison engine correctness (matching, mismatches, missing, tolerance, case sensitivity, column mismatch, one-to-many keys)
   - Pagination of results
   - Excel report generation and download
   - SSE progress events
   - Comparison history CRUD
   - Error handling (invalid files, SQL errors, timeouts, oversized uploads)

2. **Frontend — Component integration tests** (Angular `TestBed` + `HttpClientTestingModule`): Each page component (History, Compare, Results) tested with HTTP services mocked. This seam validates:
   - Configuration form rendering and validation (file upload, SQL input toggle, key/tolerance column selection, delimiter/case-sensitivity controls)
   - SSE progress display
   - Dashboard metric card rendering with correct values
   - Chart rendering with correct data
   - Side-by-side mismatch table with highlighted cells
   - Pagination controls
   - Error message rendering (inline and snackbar)
   - Excel download trigger

### Test Fixtures

- Small CSV/TXT/XLS/XLSX files covering: exact matches, mismatches, missing records, tolerance edge cases, one-to-many keys, column mismatch scenarios, non-numeric tolerance columns, case-sensitivity variations.
- These fixtures replicate the three comparison examples from the requirements document.

## Out of Scope

- **Authentication and authorization** — no login, signup, roles, or permissions for v1.
- **Multi-database support** — only PostgreSQL is supported for SQL input; MySQL, Oracle, SQL Server are excluded.
- **Dark mode / theming** — light mode only for v1.
- **Distributed / clustered deployment** — single-instance application only.
- **Dataset editing** — users cannot modify datasets within the application.
- **Saved comparison templates** — users cannot save and re-use comparison configurations.
- **Scheduled / automated comparisons** — all comparisons are manually triggered.
- **Real-time collaborative viewing** — no multi-user live dashboard sharing.
- **Internationalization (i18n)** — English only.
- **Mobile-optimized layout** — desktop-first responsive design, no dedicated mobile views.

## Further Notes

- **Tolerance semantics**: Tolerance is percentage-based and bidirectional. If either direction's adjusted range covers the other dataset's actual value, it is considered a match. Example: DS1 `score=99` with 1% tolerance → range [98.01, 99.99]. DS2 `score=100` with 1% tolerance → range [99, 101]. Since DS2's adjusted lower bound (99) equals DS1's actual value (99), this is a match.
- **"Not matching" vs "missing" relationship**: "Missing" is always a subset of "not matching." Every missing record is also counted as not-matching, but not every not-matching record is missing (some have the key but differ in non-key values).
- **DuckDB lifecycle**: A fresh in-process DuckDB connection is created per comparison. Parquet files are the persistent artifact; DuckDB state is ephemeral.
- **Angular 22 standalone components**: The frontend uses Angular's standalone component architecture exclusively — no `NgModule` declarations.
