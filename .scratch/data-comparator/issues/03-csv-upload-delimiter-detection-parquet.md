# 03 — CSV File Upload, Delimiter Detection & Parquet Conversion

**What to build:** The first real data path: a user uploads a CSV or TXT file, the backend auto-detects its delimiter, converts it to Parquet via DuckDB, stores it on disk under `data/{comparisonId}/`, and returns the comparison ID plus auto-detected column headers. The upload endpoint accepts multipart with two files (ds1/ds2) and supports delimiter override (auto, comma, tab, pipe, semicolon, custom character).

**Blocked by:** 02 — Backend Domain Model, Config & H2 Persistence

**Status:** ready-for-agent

- [ ] `POST /api/v1/comparisons/upload` accepts multipart with `ds1File`, `ds2File`, and a JSON config part specifying delimiter preference per dataset
- [ ] `DelimiterDetector` analyzes the first 10 lines, counts candidate delimiter (comma, tab, pipe, semicolon) occurrences, picks the most consistent one, defaults to comma on ambiguity
- [ ] User can override auto-detection by specifying a known delimiter or a custom single character
- [ ] `DuckDbService` converts CSV/TXT to Parquet using `read_csv_auto()` or `read_csv()` with the detected/specified delimiter
- [ ] Parquet files written to `{app.storage.path}/{comparisonId}/ds1.parquet` and `ds2.parquet`
- [ ] `DuckDbService.getColumnHeaders()` reads Parquet schema and returns column names
- [ ] Response includes `comparisonId` and `columns` (list of detected header names per dataset)
- [ ] `ComparisonRecord` status updated to UPLOADED
- [ ] File size validated against `app.upload.max-file-size`; 413 returned for oversized files
- [ ] `@SpringBootTest` + `MockMvc` tests with small CSV fixtures: comma-delimited, tab-delimited, pipe-delimited, custom delimiter, auto-detection fallback to comma
