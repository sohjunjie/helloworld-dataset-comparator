# 04 — Excel File Upload (.xls/.xlsx) & Parquet Conversion

**What to build:** Extend the upload endpoint to accept `.xls` and `.xlsx` files. The backend parses them via Apache POI, streams rows into a DuckDB temp table, and exports to Parquet — using the same storage and header-detection path as CSV uploads.

**Blocked by:** 03 — CSV File Upload, Delimiter Detection & Parquet Conversion

**Status:** ready-for-agent

- [ ] `FileParserService` detects `.xls` / `.xlsx` extension and routes to the POI parsing path
- [ ] `.xls` files parsed via `HSSFWorkbook`; rows streamed into DuckDB temp table then exported to Parquet
- [ ] `.xlsx` files parsed via streaming/event-model `XSSFReader` for memory efficiency; same DuckDB export path
- [ ] Column headers extracted from the first row of the spreadsheet
- [ ] Upload response returns the same `comparisonId` + `columns` shape as CSV uploads
- [ ] Handles edge cases: empty sheets, sheets with only headers, mixed data types in columns
- [ ] `@SpringBootTest` + `MockMvc` tests with small `.xls` and `.xlsx` fixture files verifying Parquet output and header detection
