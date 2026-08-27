# 08 — SSE Progress Events

**What to build:** Real-time progress updates pushed from the backend to any listening client via Server-Sent Events. The comparison workflow emits stage transitions (UPLOADING → CONVERTING → COMPARING → COMPLETED/FAILED) through a `ProgressService` that manages `SseEmitter` instances per comparison ID.

**Blocked by:** 06 — Core Comparison Engine (Match / Mismatch / Missing)

**Status:** done

- [x] `GET /api/v1/comparisons/{id}/events` returns an SSE stream (`text/event-stream`)
- [x] `ProgressService` maintains a concurrent map of comparison ID → list of `SseEmitter`
- [x] `SseEmitter` timeout set to 30 minutes (matching comparison timeout)
- [x] Each SSE event is JSON: `{ "stage": "COMPARING", "percent": 75 }`
- [x] Stages emitted in order: UPLOADING, CONVERTING, COMPARING, COMPLETED (or FAILED with error message)
- [x] Multiple clients can subscribe to the same comparison's progress simultaneously
- [x] Emitters cleaned up on completion, timeout, or client disconnect
- [x] `ComparisonService` calls `ProgressService.emit()` at each stage transition during the upload and execute workflows
- [x] `@SpringBootTest` + `MockMvc` test subscribes to SSE, triggers a comparison, and asserts the sequence of events received
