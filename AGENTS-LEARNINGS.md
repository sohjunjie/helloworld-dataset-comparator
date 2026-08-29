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
- **Zero External CDN Dependencies for Self-Contained Assets**: Avoid referencing external third-party CDN `<link>` or `<script>` resources (such as Google Fonts, unbundled stylesheets, or external scripts in `index.html`). Bundle all design tokens, stylesheets, icon definitions, and UI library assets (e.g., `@angular/material` SCSS mixins and local typography definitions) directly within the local build system to ensure self-contained offline execution, eliminate external runtime network dependencies, and maintain enterprise CSP compliance.


---

## 3. TypeScript, Testing & Algorithm Best Practices

- **Standalone Component Test Isolation**: In Angular standalone component testing, provide lightweight stub routing via `provideRouter([])` in test bed configuration to isolate navigation dependencies while testing presentation elements.
- **JSDOM Drag-and-Drop Test Isolation**: In Node/JSDOM component unit tests where browser `DragEvent` and `DataTransfer` globals are not natively instantiated, test dropzone handler methods directly with synthetic event object mocks to prevent DOM API reference errors.
- **Embedded Database Test Isolation**: In Spring Boot JPA test environments, configure in-memory H2 datasources with `create-drop` in test profiles/resources to ensure clean schema generation and prevent schema/enum check-constraint drift against local file-based database artifacts.
- **JDK Interface Test Isolation via Lightweight Dynamic Proxies or In-Memory Stubs**: When testing standard JDK library interfaces (such as `java.sql.Connection` or `java.sql.Statement`) across varying JVM versions, favor using embedded in-memory databases (e.g. H2) or standard JDK reflection `Proxy.newProxyInstance` instead of inline bytecode-instrumented mocks to avoid bytecode-manipulation constraints in test runners.
- **SQL-Based Schema Unification and Tolerant Field Comparison**: When performing cross-dataset diffing across mismatched schemas in columnar engines (such as DuckDB or Parquet), create unified view projections injecting `NULL AS "col"` for missing columns and compare numeric fields using `TRY_CAST(col AS DOUBLE)` with bidirectional boundary expressions (`ABS(v1 - v2) <= pct * ABS(v1) OR ABS(v1 - v2) <= pct * ABS(v2)`) to achieve resilient fallback for mixed-type and disparate-schema datasets.
- **Spring MockMvc SSE Streaming Test Isolation**: When testing Server-Sent Events endpoints that return `SseEmitter` instances configured with long timeouts, assert `request().asyncStarted()` rather than calling blocking parameterless `MvcResult.getAsyncResult()` (which defaults to waiting for the emitter's full timeout window) to avoid blocking the test runner thread.
- **Deterministic Multi-File Parquet Pagination**: When paginating across paired result Parquet files (such as DS1 and DS2 mismatch perspectives) via separate scans with `LIMIT` and `OFFSET`, always enforce an explicit `ORDER BY _row_id` sort key to guarantee deterministic row alignment and avoid cross-page pair misalignment.
- **Angular Tab Group Lazy DOM Evaluation in Unit Tests**: When testing parent components containing `mat-tab-group`, `MatTabGroup` conditionally renders only the active tab's body template into the DOM by default; assert against the rendered active tab component and verify tab header labels via `.mat-mdc-tab` rather than querying unrendered background tab DOM nodes.
- **Hierarchical Component Service Dependency Mocking**: When a parent template instantiates standalone child components that invoke shared singleton service endpoints during initialization (such as schema or metadata discovery), configure testbed mock providers with stub handlers for both the parent's and children's dependency methods to prevent unhandled mock invocation exceptions.
- **Angular Material Form Field Error State Evaluation in Unit Tests**: `<mat-error>` inside `<mat-form-field>` only renders when `ErrorStateMatcher.isErrorState()` evaluates to true (which defaults to checking `control.invalid && (control.touched || control.dirty || form.submitted)`). In standalone component unit tests with template-driven `[(ngModel)]`, provide `{ provide: ErrorStateMatcher, useClass: InstantErrorStateMatcher }` (which returns `!!(control && control.invalid)`) to guarantee that synchronous programmatic updates and validation assertions render inline errors predictably without requiring manual DOM focus/blur events.
- **DuckDB View Row ID Preservation for Deterministic Joins**: In columnar query engines like DuckDB, parallel hash joins across dataset views do not guarantee row preservation unless row order is tracked explicitly; generating deterministic row numbers (`ROW_NUMBER() OVER () AS _ds1_rn`) on the source views and ordering join and mismatch projection tables by `ORDER BY ds1._ds1_rn` guarantees exact file-line correspondence and stable pagination.
- **Apache Commons CSV Null String Mapping for Empty Fields**: When streaming parsed CSV records into SQL/Parquet writers, configuring `.setNullString("")` on `CSVFormat` ensures unquoted empty cells are parsed as SQL `NULL` rather than empty strings (`""`), maintaining schema unification consistency when comparing mixed-presence columns across datasets.




