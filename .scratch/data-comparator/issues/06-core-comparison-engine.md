# 06 — Core Comparison Engine (Match / Mismatch / Missing)

**What to build:** The heart of the application. `POST /api/v1/comparisons/{id}/execute` accepts the user's key column selection and kicks off an async comparison. The `ComparisonEngine` uses DuckDB SQL JOINs on the two Parquet files: ANTI JOINs find missing records, INNER JOIN finds matched-key pairs, and non-key column equality checks classify each pair as FULL_MATCH or MISMATCH. Results are written to result Parquet files. Summary counts are stored in H2. `GET /api/v1/comparisons/{id}` returns the summary.

**Blocked by:** 03 — CSV File Upload, Delimiter Detection & Parquet Conversion

**Status:** done

- [x] `POST /api/v1/comparisons/{id}/execute` accepts key columns (list of strings) in the request body, triggers async comparison
- [x] `ComparisonEngine` loads both Parquet files into DuckDB views
- [x] ANTI JOIN (ds1 LEFT ds2 on keys) produces missing-from-DS2 records; written to `missing_from_ds2.parquet`
- [x] ANTI JOIN (ds2 LEFT ds1 on keys) produces missing-from-DS1 records; written to `missing_from_ds1.parquet`
- [x] INNER JOIN on keys produces matched-key pairs; non-key columns compared with exact equality (NULL = NULL treated as match)
- [x] Fully matching pairs identified; mismatched pairs written to `mismatches_ds1.parquet` + `mismatches_ds2.parquet` with a shared row index for side-by-side pairing
- [x] Summary counts computed and stored in `ComparisonRecord`: ds1RecordCount, ds2RecordCount, ds1FullyMatching, ds2FullyMatching, ds1NotMatching, ds2NotMatching, ds1MissingInDs2, ds2MissingInDs1
- [x] Status transitions: UPLOADED → CONVERTING → COMPARING → COMPLETED (or FAILED with errorMessage)
- [x] Fresh in-process DuckDB connection created per comparison, closed after completion
- [x] `@SpringBootTest` + `MockMvc` end-to-end test: upload two CSV fixtures → execute comparison → verify summary counts for exact matches, mismatches, and missing records
