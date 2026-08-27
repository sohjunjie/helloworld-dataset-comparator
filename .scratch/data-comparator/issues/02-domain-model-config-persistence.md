# 02 — Backend Domain Model, Config & H2 Persistence

**What to build:** The backend's foundational layer: JPA entity (`ComparisonRecord`), enums (`ComparisonStatus`, `DataSourceType`), DTOs (`ComparisonRequest`, `ComparisonSummary`, `PagedResult`, `ColumnHeader`, `ToleranceConfig`, `DatabaseConnectionConfig`, `MismatchDetail`, `MissingDetail`), `AppProperties` config record bound to `application.yml`, Spring Data JPA repository, and CORS config for dev. A stub `POST /api/v1/comparisons` creates a `ComparisonRecord` in H2 and `GET /api/v1/comparisons` returns the history list — proving the persistence layer works end-to-end.

**Blocked by:** 01 — Project Scaffolding + Smoke Test

**Status:** ready-for-agent

- [ ] `ComparisonRecord` JPA entity maps to H2 `comparisons` table with all fields (id, status, timestamps, ds1/ds2 metadata, configJson, summary counts, errorMessage)
- [ ] `ComparisonStatus` enum: PENDING, UPLOADING, CONVERTING, COMPARING, COMPLETED, FAILED
- [ ] `DataSourceType` enum: FILE_UPLOAD, SQL_QUERY
- [ ] All DTOs defined with proper types and validation annotations where applicable
- [ ] `AppProperties` record binds `app.storage.path`, `app.upload.max-file-size`, `app.cleanup.ttl-hours`, `app.comparison.timeout-minutes`
- [ ] `ComparisonRepository` extends `JpaRepository` with a `findByCreatedAtBefore` query method
- [ ] `WebConfig` sets CORS to allow `localhost:4200` in dev
- [ ] Stub `POST /api/v1/comparisons` creates a record and returns it with status PENDING
- [ ] `GET /api/v1/comparisons` returns all records ordered by `createdAt` descending
- [ ] `GET /api/v1/comparisons/{id}` returns a single record or 404
- [ ] `@SpringBootTest` + `MockMvc` test verifies create → list → get-by-id round-trip
