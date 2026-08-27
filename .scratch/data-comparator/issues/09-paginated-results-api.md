# 09 — Paginated Results API

**What to build:** Three GET endpoints that return paginated comparison results by querying the result Parquet files via DuckDB `LIMIT/OFFSET`. Each endpoint supports page number, page size, and (for mismatches/missing) a direction parameter indicating DS1 or DS2 perspective.

**Blocked by:** 06 — Core Comparison Engine (Match / Mismatch / Missing)

**Status:** ready-for-agent

- [ ] `GET /api/v1/comparisons/{id}/results/mismatches?page=0&size=50&direction=ds1` returns a `PagedResult` with mismatch pairs — each row includes both the DS1 and DS2 values plus a list of differing column names
- [ ] `GET /api/v1/comparisons/{id}/results/missing?page=0&size=50&direction=ds1` returns records present in DS1 but missing from DS2 (direction=ds2 returns the reverse)
- [ ] `GET /api/v1/comparisons/{id}/results/matches?page=0&size=50` returns fully matching record pairs
- [ ] `PagedResult` includes: `content` (list of row maps), `page`, `size`, `totalElements`, `totalPages`
- [ ] Pagination implemented as DuckDB SQL `LIMIT {size} OFFSET {page * size}` on the result Parquet files
- [ ] Total count computed via `SELECT COUNT(*)` on the result Parquet
- [ ] Returns 404 if comparison not found or not yet completed
- [ ] `@SpringBootTest` + `MockMvc` tests: paginate through mismatches, verify page boundaries, verify direction parameter, verify empty result set
