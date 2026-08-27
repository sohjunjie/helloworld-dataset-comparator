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
}
