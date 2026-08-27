# 11 — Frontend: Configuration Page (File Upload Mode)

**What to build:** The `/compare` route — the primary user entry point. Two side-by-side dataset input panels, each with a drag-and-drop file upload zone and delimiter controls. A two-step flow: "Upload" sends files to the backend and returns column headers; then key column multi-select and tolerance configuration appear; "Compare" triggers execution and navigates to the results page. Progress bar shows SSE updates during the transition.

**Blocked by:** 03 — CSV File Upload, Delimiter Detection & Parquet Conversion; 08 — SSE Progress Events

**Status:** done

- [x] `/compare` route registered in `app.routes.ts`, lazy-loaded standalone component
- [x] `ComparisonService` (Angular HTTP client) with methods for upload, execute, get-comparison, and all results endpoints
- [x] `ProgressService` (Angular EventSource wrapper) subscribes to SSE stream and exposes an observable of progress events
- [x] Two `dataset-input` components side by side, each with a radio toggle for "Upload File" vs "SQL Query" (SQL mode is a disabled placeholder for now)
- [x] `file-dropzone` component: drag-and-drop zone with visual feedback (dragover highlight), click-to-browse fallback, accepts `.csv,.txt,.xls,.xlsx`, shows selected file name
- [x] Delimiter selector per dataset: `mat-select` with Auto-detect, Comma, Tab, Pipe, Semicolon, Custom options; "Custom" reveals a `mat-form-field` text input for a single character
- [x] "Upload" button sends multipart to `POST /api/v1/comparisons/upload`; on success, reveals the column configuration section
- [x] `column-selector` component: `mat-select multiple` populated with auto-detected headers, plus a "manual entry" text fallback for typing column names
- [x] `tolerance-config` component: dynamic list of column-name + percentage pairs with "Add" / "Remove" controls; percentage validated 0–100
- [x] Case-sensitivity toggle checkbox, default checked (case-sensitive)
- [x] "Compare" button sends key columns + tolerance config + case-sensitivity to `POST /api/v1/comparisons/{id}/execute`, subscribes to SSE, shows `mat-progress-bar`, navigates to `/results/{id}` on COMPLETED
- [x] Angular `TestBed` component test: renders the form, simulates file selection, mocks HTTP responses, verifies two-step flow
