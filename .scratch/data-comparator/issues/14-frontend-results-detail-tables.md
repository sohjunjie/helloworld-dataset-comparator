# 14 — Frontend: Results Dashboard (Detail Tables + Highlighting)

**What to build:** The detail section of the results page: tabbed views for mismatches, missing from DS2, and missing from DS1. The mismatch tab shows a side-by-side table (DS1 left, DS2 right) with differing cells highlighted. All tables use server-side pagination. The Excel download button is wired up.

**Blocked by:** 13 — Frontend: Results Dashboard (Summary + Charts)

**Status:** ready-for-agent

- [ ] `mat-tab-group` with three tabs: "Mismatches", "Missing from DS2", "Missing from DS1"
- [ ] `detail-table` reusable standalone component accepting a result type and comparison ID
- [ ] Mismatch tab: side-by-side layout — DS1 columns on the left, DS2 columns on the right, separated by a visual divider; differing cell values highlighted with orange/red background using the differing-columns metadata from the API
- [ ] Missing tabs: single table showing records from the respective dataset
- [ ] `mat-paginator` per tab with page size 50; page changes trigger `GET /api/v1/comparisons/{id}/results/{type}?page=N&size=50`
- [ ] Tables display column headers dynamically based on the dataset's schema
- [ ] "Download Report" button triggers `GET /api/v1/comparisons/{id}/report` and saves the streamed `.xlsx` file via a Blob download
- [ ] Loading spinner shown while fetching each page of results
- [ ] Angular `TestBed` component test: mocks paginated API, verifies table renders rows, verifies highlighted cells have the correct CSS class, verifies pagination triggers correct API calls, verifies download triggers
