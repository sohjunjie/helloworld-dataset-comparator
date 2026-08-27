# 13 — Frontend: Results Dashboard (Summary + Charts)

**What to build:** The `/results/:comparisonId` route. On load, subscribes to SSE for progress updates and shows a `mat-progress-bar` until COMPLETED. Once done, fetches the comparison summary and renders metric cards and a summary chart.

**Blocked by:** 09 — Paginated Results API; 11 — Frontend: Configuration Page (File Upload Mode)

**Status:** ready-for-agent

- [ ] `/results/:comparisonId` route registered, lazy-loaded standalone component
- [ ] On load, subscribes to `GET /api/v1/comparisons/{id}/events` SSE stream; shows `mat-progress-bar` with stage label (Uploading / Converting / Comparing)
- [ ] On COMPLETED event, fetches `GET /api/v1/comparisons/{id}` for summary data
- [ ] On FAILED event, displays error message from the SSE payload
- [ ] `summary-cards` component: 6 `mat-card` elements displaying DS1 record count, DS2 record count, fully matching (DS1 direction), fully matching (DS2 direction), not matching (DS1), not matching (DS2), missing from DS2, missing from DS1
- [ ] `summary-chart` component: bar or donut chart (Chart.js + ng2-charts) visualizing match/mismatch/missing distribution
- [ ] Chart data derived from the summary counts; chart is responsive and readable
- [ ] "Back to Compare" link and "Download Report" button in the toolbar (download wired in ticket 14)
- [ ] Angular `TestBed` component test: mocks SSE + API, verifies cards render with correct values, verifies chart renders
