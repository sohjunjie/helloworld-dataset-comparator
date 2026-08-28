# 15 — Frontend: History Page

**What to build:** The landing page at `/` and `/history`. A table listing all recent comparisons within the TTL window, with status indicators, source info, and actions to view results or delete a comparison. Auto-refreshes and links to the other pages.

**Blocked by:** 13 — Frontend: Results Dashboard (Summary + Charts)

**Status:** done

- [x] `/` and `/history` routes both render the history component (redirect or shared route)
- [x] `mat-table` with columns: ID (short UUID, clickable link to `/results/:id`), Created (formatted timestamp), Status (`mat-chip`: COMPLETED green, COMPARING blue, FAILED red, PENDING grey), DS1 Source (file name or "SQL Query"), DS2 Source, Records (DS1 count / DS2 count), Actions
- [x] Actions column: "View Results" icon button (navigates to `/results/:id`), "Delete" icon button (calls `DELETE /api/v1/comparisons/{id}`, removes row from table)
- [x] Data fetched from `GET /api/v1/comparisons` on init
- [x] Auto-refresh via `interval(30000)` observable re-fetching the list
- [x] "New Comparison" FAB or button navigates to `/compare`
- [x] Empty state message when no comparisons exist
- [x] App shell toolbar with "Data Comparator" title and navigation links (History, New Comparison)
- [x] Angular `TestBed` component test: mocks API, verifies table renders with correct columns, verifies navigation links, verifies delete action
