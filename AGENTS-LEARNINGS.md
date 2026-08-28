# Agents Learnings

This document records agent self-reflections, post-mortem root causes from past executions, and generic software engineering / operational best practices.

---

## 1. Post-Mortem & Agent Operational Principles

- **Windows CLI Tooling and Process Lifecycle**: Long-running background server processes (e.g. dev servers and Spring Boot instances) started via background tasks must be explicitly terminated after health/smoke verification to prevent background port locking and resource consumption.
- **Issue Tracking & Spec Synchronization**: When implementing work from issue tickets or specification files (e.g. `.scratch/**/issues/*.md`), updating the originating issue artifact (checking off completed requirement items `[x]` and updating `Status: done`) is a mandatory, atomic part of the pre-commit and task completion lifecycle.

---

## 2. Architecture, Styling & UI Best Practices

- **Dev Proxy Alignment**: Always configure dev server proxying (e.g., `proxy.conf.json`) in both `angular.json` options and startup scripts so full round-trip API calls succeed seamlessly in both CLI and browser contexts without CORS overhead.
- **Angular Material Theme & Overlay Backgrounds**: Always include `@include mat.all-component-themes($theme)` or theme mixins in global `styles.scss` and explicitly define solid background styling for `.mat-mdc-select-panel` overlays to prevent dropdown menus from rendering transparently.


---

## 3. TypeScript, Testing & Algorithm Best Practices

- **Standalone Component Test Isolation**: In Angular standalone component testing, provide lightweight stub routing via `provideRouter([])` in test bed configuration to isolate navigation dependencies while testing presentation elements.
- **JSDOM Drag-and-Drop Test Isolation**: In Node/JSDOM component unit tests where browser `DragEvent` and `DataTransfer` globals are not natively instantiated, test dropzone handler methods directly with synthetic event object mocks to prevent DOM API reference errors.
- **Embedded Database Test Isolation**: In Spring Boot JPA test environments, configure in-memory H2 datasources with `create-drop` in test profiles/resources to ensure clean schema generation and prevent schema/enum check-constraint drift against local file-based database artifacts.
- **JDK Interface Test Isolation via Lightweight Dynamic Proxies or In-Memory Stubs**: When testing standard JDK library interfaces (such as `java.sql.Connection` or `java.sql.Statement`) across varying JVM versions, favor using embedded in-memory databases (e.g. H2) or standard JDK reflection `Proxy.newProxyInstance` instead of inline bytecode-instrumented mocks to avoid bytecode-manipulation constraints in test runners.
- **SQL-Based Schema Unification and Tolerant Field Comparison**: When performing cross-dataset diffing across mismatched schemas in columnar engines (such as DuckDB or Parquet), create unified view projections injecting `NULL AS "col"` for missing columns and compare numeric fields using `TRY_CAST(col AS DOUBLE)` with bidirectional boundary expressions (`ABS(v1 - v2) <= pct * ABS(v1) OR ABS(v1 - v2) <= pct * ABS(v2)`) to achieve resilient fallback for mixed-type and disparate-schema datasets.
- **Spring MockMvc SSE Streaming Test Isolation**: When testing Server-Sent Events endpoints that return `SseEmitter` instances configured with long timeouts, assert `request().asyncStarted()` rather than calling blocking parameterless `MvcResult.getAsyncResult()` (which defaults to waiting for the emitter's full timeout window) to avoid blocking the test runner thread.
- **Deterministic Multi-File Parquet Pagination**: When paginating across paired result Parquet files (such as DS1 and DS2 mismatch perspectives) via separate scans with `LIMIT` and `OFFSET`, always enforce an explicit `ORDER BY _row_id` sort key to guarantee deterministic row alignment and avoid cross-page pair misalignment.
- **SXSSF Streaming & POI CellStyle Lifecycle**: When generating large-scale streaming Excel workbooks with Apache POI `SXSSFWorkbook`, instantiate `CellStyle` and `Font` objects once per workbook rather than per row/cell to avoid exceeding the 64,000 unique cell styles limit, and always invoke `workbook.dispose()` in addition to `close()` to immediately clean up streaming temporary disk files.


