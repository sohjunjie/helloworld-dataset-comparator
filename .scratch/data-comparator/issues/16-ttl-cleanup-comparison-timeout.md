# 16 — TTL Cleanup Scheduler + Comparison Timeout

**What to build:** Operational guardrails: a scheduled cleanup task that purges expired comparisons and their Parquet files, and a timeout mechanism that fails long-running comparisons. Both are driven by configurable properties.

**Blocked by:** 06 — Core Comparison Engine (Match / Mismatch / Missing)

**Status:** done

- [x] `CleanupService` with `@Scheduled(fixedRate = 900000)` (every 15 minutes) queries `ComparisonRepository.findByCreatedAtBefore(cutoff)` where cutoff = now minus `app.cleanup.ttl-hours`
- [x] For each expired record: delete the comparison's directory from disk (all Parquet files), then delete the H2 record
- [x] Cleanup handles missing directories gracefully (already deleted, permissions issues)
- [x] Comparison timeout: `ComparisonService` wraps the async comparison execution with a `Future.get(timeoutMinutes, MINUTES)` or equivalent; on timeout, sets status to FAILED with "Comparison timed out" message and emits FAILED SSE event
- [x] Upload size limit enforced at the Spring multipart level (`spring.servlet.multipart.max-file-size`) and returns HTTP 413 with a descriptive error message
- [x] `@SpringBootTest` test: creates a comparison with a very short TTL override, triggers cleanup, verifies the record and files are gone
- [x] `@SpringBootTest` test: verifies oversized upload returns 413
