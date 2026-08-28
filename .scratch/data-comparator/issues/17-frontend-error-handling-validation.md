# 17 — Frontend Error Handling & Validation

**What to build:** Comprehensive error handling across the frontend: field-level inline validation on the configuration form, snackbar toast notifications for backend/system errors, and graceful error state rendering on the results page for failed comparisons.

**Blocked by:** 11 — Frontend: Configuration Page (File Upload Mode); 14 — Frontend: Results Dashboard (Detail Tables + Highlighting)

**Status:** done

- [x] Configuration page: inline `mat-error` messages for — no file selected, invalid custom delimiter (empty or multi-char), no key columns selected, tolerance percentage out of 0–100 range, SQL mode: missing connection fields or empty query
- [x] "Upload" and "Compare" buttons disabled when form is invalid
- [x] Backend HTTP errors (4xx, 5xx) caught by an Angular HTTP interceptor or per-call error handler
- [x] `MatSnackBar` toast notifications for backend errors: file too large (413), SQL execution failed, connection refused, comparison timed out, unexpected server error — toast shows the error message from the response body, auto-dismisses after 5 seconds with a "Dismiss" action
- [x] Results page: if comparison status is FAILED, shows an error banner with the `errorMessage` instead of charts/tables
- [x] Results page: if comparison is still in progress (not yet COMPLETED), shows progress bar (SSE) instead of empty data
- [x] Network error handling: shows a toast for connection failures (backend not running)
- [x] Angular `TestBed` tests: verifies inline errors appear for invalid inputs, verifies snackbar shown on simulated HTTP error, verifies error banner on failed comparison

