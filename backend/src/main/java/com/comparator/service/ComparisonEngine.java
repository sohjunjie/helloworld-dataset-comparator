package com.comparator.service;

import com.comparator.model.dto.ComparisonResult;
import com.comparator.model.dto.ToleranceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ComparisonEngine {

    private static final Logger log = LoggerFactory.getLogger(ComparisonEngine.class);

    private final DuckDbService duckDbService;

    public ComparisonEngine(DuckDbService duckDbService) {
        this.duckDbService = duckDbService;
    }

    public ComparisonResult compare(
            Path ds1Parquet,
            Path ds2Parquet,
            Path outputDir,
            List<String> keyColumns,
            List<ToleranceConfig> tolerances,
            Boolean caseSensitive
    ) {
        if (ds1Parquet == null || !Files.exists(ds1Parquet)) {
            throw new IllegalArgumentException("Dataset 1 Parquet file does not exist: " + ds1Parquet);
        }
        if (ds2Parquet == null || !Files.exists(ds2Parquet)) {
            throw new IllegalArgumentException("Dataset 2 Parquet file does not exist: " + ds2Parquet);
        }
        if (keyColumns == null || keyColumns.isEmpty()) {
            throw new IllegalArgumentException("At least one key column must be specified");
        }

        List<String> ds1Headers = duckDbService.getColumnHeaders(ds1Parquet);
        List<String> ds2Headers = duckDbService.getColumnHeaders(ds2Parquet);

        for (String key : keyColumns) {
            if (!ds1Headers.contains(key)) {
                throw new IllegalArgumentException("Key column '" + key + "' not found in Dataset 1 headers: " + ds1Headers);
            }
            if (!ds2Headers.contains(key)) {
                throw new IllegalArgumentException("Key column '" + key + "' not found in Dataset 2 headers: " + ds2Headers);
            }
        }

        Map<String, Double> tolMap = new HashMap<>();
        if (tolerances != null) {
            for (ToleranceConfig tc : tolerances) {
                if (tc != null && tc.columnName() != null && !tc.columnName().isBlank()) {
                    if (tc.percentage() == null || tc.percentage() < 0.0 || tc.percentage() > 100.0) {
                        throw new IllegalArgumentException(
                                "Tolerance percentage must be between 0 and 100 for column '" + tc.columnName() + "': " + tc.percentage());
                    }
                    tolMap.put(tc.columnName(), tc.percentage());
                }
            }
        }

        boolean isCaseSensitive = caseSensitive == null || caseSensitive;

        try {
            Files.createDirectories(outputDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create output directory: " + outputDir, e);
        }

        Path missingDs2Path = outputDir.resolve("missing_from_ds2.parquet");
        Path missingDs1Path = outputDir.resolve("missing_from_ds1.parquet");
        Path matchesPath = outputDir.resolve("matches.parquet");
        Path mismatchesDs1Path = outputDir.resolve("mismatches_ds1.parquet");
        Path mismatchesDs2Path = outputDir.resolve("mismatches_ds2.parquet");

        String normDs1 = normalizePath(ds1Parquet);
        String normDs2 = normalizePath(ds2Parquet);
        String normMissingDs2 = normalizePath(missingDs2Path);
        String normMissingDs1 = normalizePath(missingDs1Path);
        String normMatches = normalizePath(matchesPath);
        String normMismatchesDs1 = normalizePath(mismatchesDs1Path);
        String normMismatchesDs2 = normalizePath(mismatchesDs2Path);

        Set<String> allColsSet = new LinkedHashSet<>(ds1Headers);
        allColsSet.addAll(ds2Headers);
        List<String> allCols = new ArrayList<>(allColsSet);

        List<String> nonKeyCols = allCols.stream()
                .filter(col -> !keyColumns.contains(col))
                .toList();

        try (Connection conn = duckDbService.createConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Create source views with unified schema (NULL columns for missing ones)
            String ds1ViewSelect = allCols.stream()
                    .map(col -> ds1Headers.contains(col) ? quote(col) : "NULL AS " + quote(col))
                    .collect(Collectors.joining(", "));
            String ds2ViewSelect = allCols.stream()
                    .map(col -> ds2Headers.contains(col) ? quote(col) : "NULL AS " + quote(col))
                    .collect(Collectors.joining(", "));

            stmt.execute(String.format("CREATE VIEW ds1 AS SELECT %s FROM read_parquet('%s')", ds1ViewSelect, normDs1));
            stmt.execute(String.format("CREATE VIEW ds2 AS SELECT %s FROM read_parquet('%s')", ds2ViewSelect, normDs2));

            // Key join conditions
            String keyJoinCondition = buildKeyJoinCondition(keyColumns, "ds1", "ds2", isCaseSensitive);
            String keyJoinConditionRev = buildKeyJoinCondition(keyColumns, "ds2", "ds1", isCaseSensitive);

            // 2. Missing from DS2 (records in DS1 not in DS2)
            stmt.execute(String.format(
                    "CREATE TABLE missing_from_ds2 AS SELECT ds1.* FROM ds1 ANTI JOIN ds2 ON %s",
                    keyJoinCondition
            ));
            stmt.execute(String.format("COPY (SELECT * FROM missing_from_ds2) TO '%s' (FORMAT PARQUET)", normMissingDs2));

            // 3. Missing from DS1 (records in DS2 not in DS1)
            stmt.execute(String.format(
                    "CREATE TABLE missing_from_ds1 AS SELECT ds2.* FROM ds2 ANTI JOIN ds1 ON %s",
                    keyJoinConditionRev
            ));
            stmt.execute(String.format("COPY (SELECT * FROM missing_from_ds1) TO '%s' (FORMAT PARQUET)", normMissingDs1));

            // 4. Matched pairs comparison
            List<String> selectFields = new ArrayList<>();
            selectFields.add("ROW_NUMBER() OVER () AS _pair_id");

            // Build match condition and diff columns expression
            String matchCondition;
            String diffColumnsExpr;

            if (nonKeyCols.isEmpty()) {
                matchCondition = "TRUE";
                diffColumnsExpr = "''";
            } else {
                List<String> colEqualityExprs = new ArrayList<>();
                List<String> diffCaseExprs = new ArrayList<>();

                for (String col : nonKeyCols) {
                    String quotedCol = quote(col);
                    String matchExpr;

                    if (tolMap.containsKey(col)) {
                        double pct = tolMap.get(col);
                        String exactFallback;
                        if (isCaseSensitive) {
                            exactFallback = String.format("(CAST(ds1.%s AS VARCHAR) = CAST(ds2.%s AS VARCHAR))", quotedCol, quotedCol);
                        } else {
                            exactFallback = String.format("(LOWER(CAST(ds1.%s AS VARCHAR)) = LOWER(CAST(ds2.%s AS VARCHAR)))", quotedCol, quotedCol);
                        }

                        matchExpr = String.format(
                                "(CASE " +
                                "WHEN ds1.%s IS NULL AND ds2.%s IS NULL THEN TRUE " +
                                "WHEN ds1.%s IS NULL OR ds2.%s IS NULL THEN FALSE " +
                                "WHEN TRY_CAST(ds1.%s AS DOUBLE) IS NOT NULL AND TRY_CAST(ds2.%s AS DOUBLE) IS NOT NULL THEN " +
                                "  ((ABS(TRY_CAST(ds1.%s AS DOUBLE) - TRY_CAST(ds2.%s AS DOUBLE)) <= ((%s / 100.0) * ABS(TRY_CAST(ds1.%s AS DOUBLE)))) " +
                                "  OR " +
                                "  (ABS(TRY_CAST(ds1.%s AS DOUBLE) - TRY_CAST(ds2.%s AS DOUBLE)) <= ((%s / 100.0) * ABS(TRY_CAST(ds2.%s AS DOUBLE))))) " +
                                "ELSE %s END)",
                                quotedCol, quotedCol,
                                quotedCol, quotedCol,
                                quotedCol, quotedCol,
                                quotedCol, quotedCol, pct, quotedCol,
                                quotedCol, quotedCol, pct, quotedCol,
                                exactFallback
                        );
                    } else {
                        if (isCaseSensitive) {
                            matchExpr = String.format("(ds1.%s IS NOT DISTINCT FROM ds2.%s)", quotedCol, quotedCol);
                        } else {
                            matchExpr = String.format(
                                    "(CASE " +
                                    "WHEN ds1.%s IS NULL AND ds2.%s IS NULL THEN TRUE " +
                                    "WHEN ds1.%s IS NULL OR ds2.%s IS NULL THEN FALSE " +
                                    "ELSE LOWER(CAST(ds1.%s AS VARCHAR)) = LOWER(CAST(ds2.%s AS VARCHAR)) END)",
                                    quotedCol, quotedCol,
                                    quotedCol, quotedCol,
                                    quotedCol, quotedCol
                            );
                        }
                    }

                    colEqualityExprs.add(matchExpr);

                    String diffCase = String.format("CASE WHEN %s THEN NULL ELSE '%s' END",
                            matchExpr, escapeSingleQuotes(col));
                    diffCaseExprs.add(diffCase);
                }

                matchCondition = String.join(" AND ", colEqualityExprs);
                diffColumnsExpr = "CONCAT_WS(',', " + String.join(", ", diffCaseExprs) + ")";
            }

            selectFields.add("(" + matchCondition + ") AS _is_full_match");
            selectFields.add("(" + diffColumnsExpr + ") AS _diff_columns");

            for (int i = 0; i < allCols.size(); i++) {
                String col = allCols.get(i);
                selectFields.add("ds1." + quote(col) + " AS " + quote("ds1_c" + i));
                selectFields.add("ds2." + quote(col) + " AS " + quote("ds2_c" + i));
            }

            String allMatchedPairsSql = String.format(
                    "CREATE TABLE all_matched_pairs AS SELECT %s FROM ds1 INNER JOIN ds2 ON %s",
                    String.join(", ", selectFields),
                    keyJoinCondition
            );
            stmt.execute(allMatchedPairsSql);

            // 5. Build matches table
            List<String> matchesSelectCols = new ArrayList<>();
            matchesSelectCols.add("ROW_NUMBER() OVER () AS _row_id");
            for (int i = 0; i < allCols.size(); i++) {
                matchesSelectCols.add(quote("ds1_c" + i) + " AS " + quote(allCols.get(i)));
            }
            stmt.execute(String.format(
                    "CREATE TABLE matches AS SELECT %s FROM all_matched_pairs WHERE _is_full_match = TRUE",
                    String.join(", ", matchesSelectCols)
            ));
            stmt.execute(String.format("COPY (SELECT * FROM matches) TO '%s' (FORMAT PARQUET)", normMatches));

            // 6. Build mismatches_ds1 table
            List<String> mismatchDs1SelectCols = new ArrayList<>();
            mismatchDs1SelectCols.add("ROW_NUMBER() OVER () AS _row_id");
            mismatchDs1SelectCols.add("_diff_columns");
            for (int i = 0; i < allCols.size(); i++) {
                mismatchDs1SelectCols.add(quote("ds1_c" + i) + " AS " + quote(allCols.get(i)));
            }
            stmt.execute(String.format(
                    "CREATE TABLE mismatches_ds1 AS SELECT %s FROM all_matched_pairs WHERE _is_full_match = FALSE",
                    String.join(", ", mismatchDs1SelectCols)
            ));
            stmt.execute(String.format("COPY (SELECT * FROM mismatches_ds1) TO '%s' (FORMAT PARQUET)", normMismatchesDs1));

            // 7. Build mismatches_ds2 table
            List<String> mismatchDs2SelectCols = new ArrayList<>();
            mismatchDs2SelectCols.add("ROW_NUMBER() OVER () AS _row_id");
            mismatchDs2SelectCols.add("_diff_columns");
            for (int i = 0; i < allCols.size(); i++) {
                mismatchDs2SelectCols.add(quote("ds2_c" + i) + " AS " + quote(allCols.get(i)));
            }
            stmt.execute(String.format(
                    "CREATE TABLE mismatches_ds2 AS SELECT %s FROM all_matched_pairs WHERE _is_full_match = FALSE",
                    String.join(", ", mismatchDs2SelectCols)
            ));
            stmt.execute(String.format("COPY (SELECT * FROM mismatches_ds2) TO '%s' (FORMAT PARQUET)", normMismatchesDs2));

            // 8. Extract counts
            long ds1RecordCount = getCount(stmt, "SELECT COUNT(*) FROM ds1");
            long ds2RecordCount = getCount(stmt, "SELECT COUNT(*) FROM ds2");
            long ds1MissingInDs2 = getCount(stmt, "SELECT COUNT(*) FROM missing_from_ds2");
            long ds2MissingInDs1 = getCount(stmt, "SELECT COUNT(*) FROM missing_from_ds1");
            long matchingCount = getCount(stmt, "SELECT COUNT(*) FROM matches");
            long mismatchCount = getCount(stmt, "SELECT COUNT(*) FROM mismatches_ds1");

            return new ComparisonResult(
                    ds1RecordCount,
                    ds2RecordCount,
                    matchingCount,
                    matchingCount,
                    mismatchCount,
                    mismatchCount,
                    ds1MissingInDs2,
                    ds2MissingInDs1
            );
        } catch (SQLException e) {
            log.error("DuckDB comparison execution failed: {}", e.getMessage(), e);
            throw new RuntimeException("Comparison engine failed: " + e.getMessage(), e);
        }
    }

    private String buildKeyJoinCondition(List<String> keyColumns, String leftAlias, String rightAlias, boolean isCaseSensitive) {
        if (isCaseSensitive) {
            return keyColumns.stream()
                    .map(k -> String.format("%s.%s IS NOT DISTINCT FROM %s.%s", leftAlias, quote(k), rightAlias, quote(k)))
                    .collect(Collectors.joining(" AND "));
        } else {
            return keyColumns.stream()
                    .map(k -> String.format(
                            "(%s.%s IS NOT DISTINCT FROM %s.%s OR (%s.%s IS NOT NULL AND %s.%s IS NOT NULL AND LOWER(CAST(%s.%s AS VARCHAR)) = LOWER(CAST(%s.%s AS VARCHAR))))",
                            leftAlias, quote(k), rightAlias, quote(k),
                            leftAlias, quote(k), rightAlias, quote(k),
                            leftAlias, quote(k), rightAlias, quote(k)))
                    .collect(Collectors.joining(" AND "));
        }
    }

    private long getCount(Statement stmt, String sql) throws SQLException {
        try (ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        }
    }

    private String quote(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private String escapeSingleQuotes(String str) {
        return str.replace("'", "''");
    }

    private String normalizePath(Path path) {
        return path.toAbsolutePath().toString().replace('\\', '/').replace("'", "''");
    }
}
