# 10 — Excel Report Generation & Download

**What to build:** A downloadable, nicely formatted Excel report of the comparison results. The report is generated on-demand using Apache POI SXSSF (streaming API) for low-memory creation, streamed to the client as a `.xlsx` download.

**Blocked by:** 09 — Paginated Results API

**Status:** done

- [x] `GET /api/v1/comparisons/{id}/report` returns `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` with `Content-Disposition: attachment; filename="comparison-{id}.xlsx"`
- [x] **Summary sheet** (first): comparison ID, timestamp, configuration snapshot, all summary counts (DS1 count, DS2 count, matching, not-matching, missing per direction)
- [x] **Mismatches (DS1→DS2)** sheet: DS1 records alongside their DS2 counterparts, differing cells highlighted with orange fill
- [x] **Mismatches (DS2→DS1)** sheet: reverse direction if directionally distinct
- [x] **Missing from DS2** sheet: records present in DS1 but absent from DS2
- [x] **Missing from DS1** sheet: records present in DS2 but absent from DS1
- [x] Detail sheets split into multiple sheets ("Mismatches (DS1→DS2) (1)", "(2)", …) when exceeding ~1,048,576 rows
- [x] Header rows frozen (`createFreezePane`), bold, with auto-filter enabled
- [x] Generated using SXSSF with a sliding window (e.g., 100 rows) to keep memory low
- [x] Returns 404 if comparison not found; 409 if comparison not yet completed
- [x] `@SpringBootTest` + `MockMvc` test: triggers report download, validates the response is a valid `.xlsx` with expected sheet names and row counts
