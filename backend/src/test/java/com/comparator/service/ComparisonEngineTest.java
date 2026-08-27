package com.comparator.service;

import com.comparator.model.dto.ComparisonResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ComparisonEngineTest {

    private DuckDbService duckDbService;
    private ComparisonEngine comparisonEngine;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        duckDbService = new DuckDbService();
        comparisonEngine = new ComparisonEngine(duckDbService);
    }

    @Test
    @DisplayName("Should detect 100% full matches when datasets are identical")
    void shouldDetectFullMatchesWhenIdentical() throws Exception {
        Path ds1Csv = tempDir.resolve("ds1.csv");
        Path ds2Csv = tempDir.resolve("ds2.csv");
        Files.writeString(ds1Csv, "id,name,age\n1,Alice,30\n2,Bob,25\n3,Charlie,35\n");
        Files.writeString(ds2Csv, "id,name,age\n1,Alice,30\n2,Bob,25\n3,Charlie,35\n");

        Path ds1Parquet = tempDir.resolve("ds1.parquet");
        Path ds2Parquet = tempDir.resolve("ds2.parquet");
        duckDbService.csvToParquet(ds1Csv, ds1Parquet, ',');
        duckDbService.csvToParquet(ds2Csv, ds2Parquet, ',');

        ComparisonResult result = comparisonEngine.compare(
                ds1Parquet, ds2Parquet, tempDir, List.of("id"), List.of(), false
        );

        assertThat(result.ds1RecordCount()).isEqualTo(3);
        assertThat(result.ds2RecordCount()).isEqualTo(3);
        assertThat(result.ds1FullyMatching()).isEqualTo(3);
        assertThat(result.ds2FullyMatching()).isEqualTo(3);
        assertThat(result.ds1NotMatching()).isEqualTo(0);
        assertThat(result.ds2NotMatching()).isEqualTo(0);
        assertThat(result.ds1MissingInDs2()).isEqualTo(0);
        assertThat(result.ds2MissingInDs1()).isEqualTo(0);

        assertThat(Files.exists(tempDir.resolve("matches.parquet"))).isTrue();
        assertThat(Files.exists(tempDir.resolve("mismatches_ds1.parquet"))).isTrue();
        assertThat(Files.exists(tempDir.resolve("mismatches_ds2.parquet"))).isTrue();
        assertThat(Files.exists(tempDir.resolve("missing_from_ds1.parquet"))).isTrue();
        assertThat(Files.exists(tempDir.resolve("missing_from_ds2.parquet"))).isTrue();
    }

    @Test
    @DisplayName("Should detect mismatches and missing records across datasets")
    void shouldDetectMismatchesAndMissing() throws Exception {
        Path ds1Csv = tempDir.resolve("ds1.csv");
        Path ds2Csv = tempDir.resolve("ds2.csv");
        // ds1: 1 (match), 2 (mismatch), 3 (missing from ds2)
        Files.writeString(ds1Csv, "id,name,amount\n1,Alice,100\n2,Bob,200\n3,Charlie,300\n");
        // ds2: 1 (match), 2 (mismatch name/amount), 4 (missing from ds1)
        Files.writeString(ds2Csv, "id,name,amount\n1,Alice,100\n2,Robert,250\n4,David,400\n");

        Path ds1Parquet = tempDir.resolve("ds1.parquet");
        Path ds2Parquet = tempDir.resolve("ds2.parquet");
        duckDbService.csvToParquet(ds1Csv, ds1Parquet, ',');
        duckDbService.csvToParquet(ds2Csv, ds2Parquet, ',');

        ComparisonResult result = comparisonEngine.compare(
                ds1Parquet, ds2Parquet, tempDir, List.of("id"), List.of(), false
        );

        assertThat(result.ds1RecordCount()).isEqualTo(3);
        assertThat(result.ds2RecordCount()).isEqualTo(3);
        assertThat(result.ds1FullyMatching()).isEqualTo(1);
        assertThat(result.ds2FullyMatching()).isEqualTo(1);
        assertThat(result.ds1NotMatching()).isEqualTo(1);
        assertThat(result.ds2NotMatching()).isEqualTo(1);
        assertThat(result.ds1MissingInDs2()).isEqualTo(1);
        assertThat(result.ds2MissingInDs1()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should support composite keys")
    void shouldSupportCompositeKeys() throws Exception {
        Path ds1Csv = tempDir.resolve("ds1.csv");
        Path ds2Csv = tempDir.resolve("ds2.csv");
        Files.writeString(ds1Csv, "dept,emp_id,salary\nENG,101,5000\nENG,102,6000\nHR,101,4500\n");
        Files.writeString(ds2Csv, "dept,emp_id,salary\nENG,101,5000\nENG,102,6500\nFIN,101,7000\n");

        Path ds1Parquet = tempDir.resolve("ds1.parquet");
        Path ds2Parquet = tempDir.resolve("ds2.parquet");
        duckDbService.csvToParquet(ds1Csv, ds1Parquet, ',');
        duckDbService.csvToParquet(ds2Csv, ds2Parquet, ',');

        ComparisonResult result = comparisonEngine.compare(
                ds1Parquet, ds2Parquet, tempDir, List.of("dept", "emp_id"), List.of(), false
        );

        assertThat(result.ds1RecordCount()).isEqualTo(3);
        assertThat(result.ds2RecordCount()).isEqualTo(3);
        assertThat(result.ds1FullyMatching()).isEqualTo(1); // ENG 101
        assertThat(result.ds1NotMatching()).isEqualTo(1);    // ENG 102
        assertThat(result.ds1MissingInDs2()).isEqualTo(1);   // HR 101
        assertThat(result.ds2MissingInDs1()).isEqualTo(1);   // FIN 101
    }

    @Test
    @DisplayName("Should treat NULL = NULL as match and NULL != non-NULL as mismatch")
    void shouldHandleNullValues() throws Exception {
        Path ds1Csv = tempDir.resolve("ds1.csv");
        Path ds2Csv = tempDir.resolve("ds2.csv");
        Files.writeString(ds1Csv, "id,notes\n1,\n2,has-note\n");
        Files.writeString(ds2Csv, "id,notes\n1,\n2,\n");

        Path ds1Parquet = tempDir.resolve("ds1.parquet");
        Path ds2Parquet = tempDir.resolve("ds2.parquet");
        duckDbService.csvToParquet(ds1Csv, ds1Parquet, ',');
        duckDbService.csvToParquet(ds2Csv, ds2Parquet, ',');

        ComparisonResult result = comparisonEngine.compare(
                ds1Parquet, ds2Parquet, tempDir, List.of("id"), List.of(), false
        );

        assertThat(result.ds1FullyMatching()).isEqualTo(1); // row 1 (null == null)
        assertThat(result.ds1NotMatching()).isEqualTo(1);    // row 2 (has-note != null)
    }

    @Test
    @DisplayName("Should throw when key column does not exist in dataset")
    void shouldThrowWhenKeyColumnMissing() throws Exception {
        Path ds1Csv = tempDir.resolve("ds1.csv");
        Path ds2Csv = tempDir.resolve("ds2.csv");
        Files.writeString(ds1Csv, "id,name\n1,Alice\n");
        Files.writeString(ds2Csv, "other_id,name\n1,Alice\n");

        Path ds1Parquet = tempDir.resolve("ds1.parquet");
        Path ds2Parquet = tempDir.resolve("ds2.parquet");
        duckDbService.csvToParquet(ds1Csv, ds1Parquet, ',');
        duckDbService.csvToParquet(ds2Csv, ds2Parquet, ',');

        assertThatThrownBy(() -> comparisonEngine.compare(
                ds1Parquet, ds2Parquet, tempDir, List.of("id"), List.of(), false
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Dataset 2");
    }

    @Test
    @DisplayName("Should match within numeric tolerance boundaries and detect outside-boundary mismatches")
    void shouldMatchWithinNumericToleranceAndDetectBoundaryMismatches() throws Exception {
        Path ds1Csv = tempDir.resolve("ds1.csv");
        Path ds2Csv = tempDir.resolve("ds2.csv");
        // row 1: 100 vs 105 -> exactly 5% on DS1 -> match
        // row 2: 100 vs 95 -> exactly 5% on DS1 -> match
        // row 3: 100 vs 105.26 -> within 5% on DS2 (105.26 * 0.05 = 5.263, diff = 5.26) -> match
        // row 4: 100 vs 106 -> outside 5% -> mismatch
        Files.writeString(ds1Csv, "id,score\n1,100\n2,100\n3,100\n4,100\n");
        Files.writeString(ds2Csv, "id,score\n1,105\n2,95\n3,105.26\n4,106\n");

        Path ds1Parquet = tempDir.resolve("ds1.parquet");
        Path ds2Parquet = tempDir.resolve("ds2.parquet");
        duckDbService.csvToParquet(ds1Csv, ds1Parquet, ',');
        duckDbService.csvToParquet(ds2Csv, ds2Parquet, ',');

        com.comparator.model.dto.ToleranceConfig tol = new com.comparator.model.dto.ToleranceConfig("score", 5.0);
        ComparisonResult result = comparisonEngine.compare(
                ds1Parquet, ds2Parquet, tempDir, List.of("id"), List.of(tol), true
        );

        assertThat(result.ds1RecordCount()).isEqualTo(4);
        assertThat(result.ds2RecordCount()).isEqualTo(4);
        assertThat(result.ds1FullyMatching()).isEqualTo(3);
        assertThat(result.ds2FullyMatching()).isEqualTo(3);
        assertThat(result.ds1NotMatching()).isEqualTo(1);
        assertThat(result.ds2NotMatching()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should fallback to exact match when tolerance column contains non-numeric strings")
    void shouldFallbackToExactMatchWhenToleranceColumnContainsNonNumeric() throws Exception {
        Path ds1Csv = tempDir.resolve("ds1.csv");
        Path ds2Csv = tempDir.resolve("ds2.csv");
        // row 1: 'N/A' vs 'N/A' -> non-numeric, exact match -> match
        // row 2: 'N/A' vs '100' -> non-numeric fallback -> mismatch
        Files.writeString(ds1Csv, "id,score\n1,N/A\n2,N/A\n");
        Files.writeString(ds2Csv, "id,score\n1,N/A\n2,100\n");

        Path ds1Parquet = tempDir.resolve("ds1.parquet");
        Path ds2Parquet = tempDir.resolve("ds2.parquet");
        duckDbService.csvToParquet(ds1Csv, ds1Parquet, ',');
        duckDbService.csvToParquet(ds2Csv, ds2Parquet, ',');

        com.comparator.model.dto.ToleranceConfig tol = new com.comparator.model.dto.ToleranceConfig("score", 10.0);
        ComparisonResult result = comparisonEngine.compare(
                ds1Parquet, ds2Parquet, tempDir, List.of("id"), List.of(tol), true
        );

        assertThat(result.ds1FullyMatching()).isEqualTo(1);
        assertThat(result.ds1NotMatching()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should validate tolerance percentage range between 0 and 100")
    void shouldValidateTolerancePercentageRange() throws Exception {
        Path ds1Csv = tempDir.resolve("ds1.csv");
        Path ds2Csv = tempDir.resolve("ds2.csv");
        Files.writeString(ds1Csv, "id,val\n1,10\n");
        Files.writeString(ds2Csv, "id,val\n1,10\n");

        Path ds1Parquet = tempDir.resolve("ds1.parquet");
        Path ds2Parquet = tempDir.resolve("ds2.parquet");
        duckDbService.csvToParquet(ds1Csv, ds1Parquet, ',');
        duckDbService.csvToParquet(ds2Csv, ds2Parquet, ',');

        com.comparator.model.dto.ToleranceConfig negativeTol = new com.comparator.model.dto.ToleranceConfig("val", -1.0);
        assertThatThrownBy(() -> comparisonEngine.compare(
                ds1Parquet, ds2Parquet, tempDir, List.of("id"), List.of(negativeTol), true
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("between 0 and 100");

        com.comparator.model.dto.ToleranceConfig overHundredTol = new com.comparator.model.dto.ToleranceConfig("val", 105.0);
        assertThatThrownBy(() -> comparisonEngine.compare(
                ds1Parquet, ds2Parquet, tempDir, List.of("id"), List.of(overHundredTol), true
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("between 0 and 100");
    }

    @Test
    @DisplayName("Should handle case-insensitive string comparison when toggle is enabled")
    void shouldHandleCaseInsensitiveStringComparison() throws Exception {
        Path ds1Csv = tempDir.resolve("ds1.csv");
        Path ds2Csv = tempDir.resolve("ds2.csv");
        Files.writeString(ds1Csv, "id,name,city\nK1,Alice,New York\nK2,Bob,London\n");
        Files.writeString(ds2Csv, "id,name,city\nk1,ALICE,NEW YORK\nk2,bob,LONDON\n");

        Path ds1Parquet = tempDir.resolve("ds1.parquet");
        Path ds2Parquet = tempDir.resolve("ds2.parquet");
        duckDbService.csvToParquet(ds1Csv, ds1Parquet, ',');
        duckDbService.csvToParquet(ds2Csv, ds2Parquet, ',');

        // Case-insensitive (caseSensitive = false) -> K1=k1, ALICE=Alice, NEW YORK=New York
        ComparisonResult caseInsensitiveResult = comparisonEngine.compare(
                ds1Parquet, ds2Parquet, tempDir, List.of("id"), List.of(), false
        );
        assertThat(caseInsensitiveResult.ds1FullyMatching()).isEqualTo(2);
        assertThat(caseInsensitiveResult.ds1NotMatching()).isEqualTo(0);

        // Case-sensitive (caseSensitive = true) -> K1 != k1, so both are missing in the other dataset
        ComparisonResult caseSensitiveResult = comparisonEngine.compare(
                ds1Parquet, ds2Parquet, tempDir, List.of("id"), List.of(), true
        );
        assertThat(caseSensitiveResult.ds1FullyMatching()).isEqualTo(0);
        assertThat(caseSensitiveResult.ds1MissingInDs2()).isEqualTo(2);
        assertThat(caseSensitiveResult.ds2MissingInDs1()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should unify schemas when column sets differ between datasets")
    void shouldUnifySchemasWhenColumnSetsDiffer() throws Exception {
        Path ds1Csv = tempDir.resolve("ds1.csv");
        Path ds2Csv = tempDir.resolve("ds2.csv");
        // DS1 has [id, name, ds1_only]
        // DS2 has [id, name, ds2_only]
        // Row 1: ds1_only has 'extra' vs NULL in DS2 -> mismatch
        // Row 2: ds1_only is empty (NULL) and ds2_only is empty (NULL) -> full match!
        Files.writeString(ds1Csv, "id,name,ds1_only\n1,Alice,extra\n2,Bob,\n");
        Files.writeString(ds2Csv, "id,name,ds2_only\n1,Alice,other\n2,Bob,\n");

        Path ds1Parquet = tempDir.resolve("ds1.parquet");
        Path ds2Parquet = tempDir.resolve("ds2.parquet");
        duckDbService.csvToParquet(ds1Csv, ds1Parquet, ',');
        duckDbService.csvToParquet(ds2Csv, ds2Parquet, ',');

        ComparisonResult result = comparisonEngine.compare(
                ds1Parquet, ds2Parquet, tempDir, List.of("id"), List.of(), true
        );

        assertThat(result.ds1RecordCount()).isEqualTo(2);
        assertThat(result.ds2RecordCount()).isEqualTo(2);
        assertThat(result.ds1FullyMatching()).isEqualTo(1); // row 2 matches (both NULL for mismatched columns)
        assertThat(result.ds1NotMatching()).isEqualTo(1);    // row 1 mismatches
    }

    @Test
    @DisplayName("Should handle duplicate keys with cartesian product per key")
    void shouldHandleDuplicateKeysCrossComparison() throws Exception {
        Path ds1Csv = tempDir.resolve("ds1.csv");
        Path ds2Csv = tempDir.resolve("ds2.csv");
        // DS1: 2 records for id=1 (val=10, val=20)
        // DS2: 2 records for id=1 (val=10, val=30)
        Files.writeString(ds1Csv, "id,val\n1,10\n1,20\n");
        Files.writeString(ds2Csv, "id,val\n1,10\n1,30\n");

        Path ds1Parquet = tempDir.resolve("ds1.parquet");
        Path ds2Parquet = tempDir.resolve("ds2.parquet");
        duckDbService.csvToParquet(ds1Csv, ds1Parquet, ',');
        duckDbService.csvToParquet(ds2Csv, ds2Parquet, ',');

        ComparisonResult result = comparisonEngine.compare(
                ds1Parquet, ds2Parquet, tempDir, List.of("id"), List.of(), true
        );

        assertThat(result.ds1RecordCount()).isEqualTo(2);
        assertThat(result.ds2RecordCount()).isEqualTo(2);
        // (1,10) x (1,10) = match
        // (1,10) x (1,30) = mismatch
        // (1,20) x (1,10) = mismatch
        // (1,20) x (1,30) = mismatch
        assertThat(result.ds1FullyMatching()).isEqualTo(1);
        assertThat(result.ds2FullyMatching()).isEqualTo(1);
        assertThat(result.ds1NotMatching()).isEqualTo(3);
        assertThat(result.ds2NotMatching()).isEqualTo(3);
    }
}
