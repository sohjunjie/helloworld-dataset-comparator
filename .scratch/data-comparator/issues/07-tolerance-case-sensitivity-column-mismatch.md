# 07 — Comparison Engine: Tolerance, Case-Sensitivity & Column Mismatch

**What to build:** Extend the comparison engine to handle three advanced comparison modes: (1) percentage-based bidirectional tolerance for numeric columns, (2) case-insensitive string comparison toggle, and (3) schema unification when datasets have different column sets. Also verify one-to-many key matching works correctly with duplicates.

**Blocked by:** 06 — Core Comparison Engine (Match / Mismatch / Missing)

**Status:** done

- [x] `POST /api/v1/comparisons/{id}/execute` also accepts tolerance config (list of column-name + percentage pairs) and a case-sensitivity boolean
- [x] Tolerance: for each tolerance column, if both values parse as numeric, match when `ABS(ds1.col - ds2.col) <= (pct/100) * ABS(ds1.col)` OR `ABS(ds1.col - ds2.col) <= (pct/100) * ABS(ds2.col)` (bidirectional)
- [x] Tolerance: when either value is non-numeric in a tolerance column, falls back to exact-match comparison
- [x] Tolerance percentages validated: 0% ≤ value ≤ 100%
- [x] Case-insensitive toggle: when enabled, all string column comparisons use `LOWER()` on both sides
- [x] Column mismatch: when DS1 and DS2 have different column sets, the engine adds NULL columns to unify schemas before comparison; non-NULL vs NULL is classified as a mismatch
- [x] One-to-many: when multiple records share the same key within a dataset, each is cross-compared (INNER JOIN naturally produces the cartesian product per key)
- [x] `@SpringBootTest` + `MockMvc` tests for: tolerance match at boundary, tolerance non-numeric fallback, case-insensitive comparison, mismatched column schemas, duplicate-key cross-comparison
