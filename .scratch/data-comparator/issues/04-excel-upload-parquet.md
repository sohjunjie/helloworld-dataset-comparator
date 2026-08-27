# 04 — Excel File Upload (.xls/.xlsx) & Parquet Conversion

**What to build:** Extend the upload endpoint to accept `.xls` and `.xlsx` files. The backend parses them via Apache POI, streams rows into a DuckDB temp table, and exports to Parquet — using the same storage and header-detection path as CSV uploads.

**Blocked by:** 03 — CSV File Upload, Delimiter Detection & Parquet Conversion

**Status:** done

- [x] `FileParserService` detects `.xls` / `.xlsx` extension and routes to the POI parsing path
- [x] `.xls` files parsed via `HSSFWorkbook`; rows streamed into DuckDB temp table then exported to Parquet
- [x] `.xlsx` files parsed via streaming/event-model `XSSFReader` for memory efficiency; same DuckDB export path
- [x] Column headers extracted from the first row of the spreadsheet
- [x] Upload response returns the same `comparisonId` + `columns` shape as CSV uploads
- [x] Handles edge cases: empty sheets, sheets with only headers, mixed data types in columns
- [x] `@SpringBootTest` + `MockMvc` tests with small `.xls` and `.xlsx` fixture files verifying Parquet output and header detection
