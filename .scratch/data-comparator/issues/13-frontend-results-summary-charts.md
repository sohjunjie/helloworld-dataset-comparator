# 13 — Frontend: Results Dashboard (Summary + Charts)

**What to build:** The `/results/:comparisonId` route. On load, subscribes to SSE for progress updates and shows a `mat-progress-bar` until COMPLETED. Once done, fetches the comparison summary and renders metric cards and a summary chart.

**Blocked by:** 09 — Paginated Results API; 11 — Frontend: Configuration Page (File Upload Mode)

**Status:** done

- [x] `/results/:comparisonId` route registered, lazy-loaded standalone component
- [x] On load, subscribes to `GET /api/v1/comparisons/{id}/events` SSE stream; shows `mat-progress-bar` with stage label (Uploading / Converting / Comparing)
- [x] On COMPLETED event, fetches `GET /api/v1/comparisons/{id}` for summary data
- [x] On FAILED event, displays error message from the SSE payload
- [x] `summary-cards` component: 6 `mat-card` elements displaying DS1 record count, DS2 record count, fully matching (DS1 direction), fully matching (DS2 direction), not matching (DS1), not matching (DS2), missing from DS2, missing from DS1
- [x] `summary-chart` component: bar or donut chart (Chart.js + ng2-charts) visualizing match/mismatch/missing distribution
- [x] Chart data derived from the summary counts; chart is responsive and readable
- [x] "Back to Compare" link and "Download Report" button in the toolbar (download wired in ticket 14)
- [x] Angular `TestBed` component test: mocks SSE + API, verifies cards render with correct values, verifies chart renders
