package com.comparator.service;

import com.comparator.config.AppProperties;
import com.comparator.model.dto.ComparisonExecuteRequest;
import com.comparator.model.dto.ComparisonResult;
import com.comparator.model.entity.ComparisonRecord;
import com.comparator.model.enums.ComparisonStatus;
import com.comparator.repository.ComparisonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComparisonServiceTest {

    @Mock
    private ComparisonRepository comparisonRepository;

    @Mock
    private ComparisonEngine comparisonEngine;

    @Mock
    private DuckDbService duckDbService;

    @Mock
    private ProgressService progressService;

    private AppProperties appProperties;
    private ObjectMapper objectMapper;
    private ComparisonService comparisonService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties(
                new AppProperties.StorageProperties(tempDir.toString()),
                new AppProperties.UploadProperties("500MB"),
                new AppProperties.CleanupProperties(1),
                new AppProperties.ComparisonProperties(30)
        );
        objectMapper = new ObjectMapper();
        comparisonService = new ComparisonService(comparisonRepository, comparisonEngine, duckDbService, progressService, appProperties, objectMapper);
    }

    @Test
    @DisplayName("Should successfully execute comparison and update status to COMPLETED")
    void shouldSuccessfullyExecuteComparison() throws Exception {
        String comparisonId = "test-id-123";
        Path compDir = tempDir.resolve(comparisonId);
        Files.createDirectories(compDir);
        Files.createFile(compDir.resolve("ds1.parquet"));
        Files.createFile(compDir.resolve("ds2.parquet"));

        ComparisonRecord record = new ComparisonRecord();
        record.setId(comparisonId);
        record.setStatus(ComparisonStatus.UPLOADED);

        when(comparisonRepository.findById(comparisonId)).thenReturn(Optional.of(record));
        when(comparisonRepository.save(any(ComparisonRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        ComparisonResult result = new ComparisonResult(10, 10, 8, 8, 1, 1, 1, 1);
        when(comparisonEngine.compare(any(), any(), any(), eq(List.of("id")), any(), any())).thenReturn(result);

        ComparisonExecuteRequest request = new ComparisonExecuteRequest(List.of("id"));
        CompletableFuture<ComparisonRecord> future = comparisonService.executeComparisonAsync(comparisonId, request);
        ComparisonRecord completed = future.get();

        assertThat(completed.getStatus()).isEqualTo(ComparisonStatus.COMPLETED);
        assertThat(completed.getDs1RecordCount()).isEqualTo(10L);
        assertThat(completed.getDs1FullyMatching()).isEqualTo(8L);
        assertThat(completed.getDs1NotMatching()).isEqualTo(1L);
        assertThat(completed.getDs1MissingInDs2()).isEqualTo(1L);
        assertThat(completed.getCompletedAt()).isNotNull();

        verify(progressService).emit(comparisonId, "COMPARING", 25);
        verify(progressService).emit(comparisonId, "COMPARING", 50);
        verify(progressService).emit(comparisonId, "COMPLETED", 100);
    }

    @Test
    @DisplayName("Should update status to FAILED when comparison engine throws")
    void shouldSetStatusFailedOnError() throws Exception {
        String comparisonId = "fail-id-123";
        Path compDir = tempDir.resolve(comparisonId);
        Files.createDirectories(compDir);
        Files.createFile(compDir.resolve("ds1.parquet"));
        Files.createFile(compDir.resolve("ds2.parquet"));

        ComparisonRecord record = new ComparisonRecord();
        record.setId(comparisonId);
        record.setStatus(ComparisonStatus.UPLOADED);

        when(comparisonRepository.findById(comparisonId)).thenReturn(Optional.of(record));
        when(comparisonRepository.save(any(ComparisonRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        when(comparisonEngine.compare(any(), any(), any(), eq(List.of("id")), any(), any()))
                .thenThrow(new RuntimeException("DuckDB syntax error"));

        ComparisonExecuteRequest request = new ComparisonExecuteRequest(List.of("id"));
        CompletableFuture<ComparisonRecord> future = comparisonService.executeComparisonAsync(comparisonId, request);
        ComparisonRecord failed = future.get();

        assertThat(failed.getStatus()).isEqualTo(ComparisonStatus.FAILED);
        assertThat(failed.getErrorMessage()).contains("DuckDB syntax error");
        assertThat(failed.getCompletedAt()).isNotNull();

        verify(progressService).emit(comparisonId, "COMPARING", 25);
        verify(progressService).emit(comparisonId, "COMPARING", 50);
        verify(progressService).emit(comparisonId, "FAILED", 100, "DuckDB syntax error");
    }

    @Test
    @DisplayName("Should throw 404 when comparison not found")
    void shouldThrow404WhenNotFound() {
        when(comparisonRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comparisonService.executeComparisonAsync("missing-id", new ComparisonExecuteRequest(List.of("id"))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("Should throw 400 when key columns empty")
    void shouldThrow400WhenKeyColumnsEmpty() {
        ComparisonRecord record = new ComparisonRecord();
        record.setId("valid-id");
        when(comparisonRepository.findById("valid-id")).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> comparisonService.executeComparisonAsync("valid-id", new ComparisonExecuteRequest(List.of())))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    @DisplayName("Should throw 404 when querying results for uncompleted comparison")
    void shouldThrow404WhenQueryingUncompletedComparison() {
        ComparisonRecord record = new ComparisonRecord();
        record.setId("uncompleted-id");
        record.setStatus(ComparisonStatus.COMPARING);
        when(comparisonRepository.findById("uncompleted-id")).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> comparisonService.getMismatches("uncompleted-id", 0, 50, "ds1"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");

        assertThatThrownBy(() -> comparisonService.getMissing("uncompleted-id", 0, 50, "ds1"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");

        assertThatThrownBy(() -> comparisonService.getMatches("uncompleted-id", 0, 50))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("Should paginate mismatches and extract differingColumns and keyValues")
    void shouldPaginateMismatches() throws Exception {
        String id = "completed-comp-1";
        Path compDir = tempDir.resolve(id);
        Files.createDirectories(compDir);
        Files.createFile(compDir.resolve("mismatches_ds1.parquet"));
        Files.createFile(compDir.resolve("mismatches_ds2.parquet"));

        ComparisonRecord record = new ComparisonRecord();
        record.setId(id);
        record.setStatus(ComparisonStatus.COMPLETED);
        record.setConfigJson("{\"keyColumns\":[\"id\"]}");
        when(comparisonRepository.findById(id)).thenReturn(Optional.of(record));

        when(duckDbService.countParquet(compDir.resolve("mismatches_ds1.parquet"))).thenReturn(5L);

        java.util.Map<String, Object> r1 = new java.util.LinkedHashMap<>();
        r1.put("_row_id", 1L);
        r1.put("_diff_columns", "score,status");
        r1.put("id", 101);
        r1.put("score", 95);
        r1.put("status", "ACTIVE");

        java.util.Map<String, Object> r2 = new java.util.LinkedHashMap<>();
        r2.put("_row_id", 1L);
        r2.put("_diff_columns", "score,status");
        r2.put("id", 101);
        r2.put("score", 98);
        r2.put("status", "INACTIVE");

        when(duckDbService.queryParquet(compDir.resolve("mismatches_ds1.parquet"), "_row_id", 0, 10))
                .thenReturn(List.of(r1));
        when(duckDbService.queryParquet(compDir.resolve("mismatches_ds2.parquet"), "_row_id", 0, 10))
                .thenReturn(List.of(r2));

        var result = comparisonService.getMismatches(id, 0, 10, "ds1");

        assertThat(result.totalElements()).isEqualTo(5L);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.last()).isTrue();
        assertThat(result.content()).hasSize(1);

        var item = result.content().getFirst();
        assertThat(item.keyValues()).containsEntry("id", 101);
        assertThat(item.ds1Values()).containsEntry("score", 95);
        assertThat(item.ds2Values()).containsEntry("score", 98);
        assertThat(item.differingColumns()).containsExactly("score", "status");
    }

    @Test
    @DisplayName("Should paginate missing records for both DS1 and DS2 directions")
    void shouldPaginateMissingRecords() throws Exception {
        String id = "completed-comp-missing";
        Path compDir = tempDir.resolve(id);
        Files.createDirectories(compDir);
        Path missingFromDs2 = compDir.resolve("missing_from_ds2.parquet");
        Path missingFromDs1 = compDir.resolve("missing_from_ds1.parquet");
        Files.createFile(missingFromDs2);
        Files.createFile(missingFromDs1);

        ComparisonRecord record = new ComparisonRecord();
        record.setId(id);
        record.setStatus(ComparisonStatus.COMPLETED);
        record.setConfigJson("{\"keyColumns\":[\"id\"]}");
        when(comparisonRepository.findById(id)).thenReturn(Optional.of(record));

        // direction ds1 -> missing_from_ds2.parquet
        when(duckDbService.countParquet(missingFromDs2)).thenReturn(1L);
        java.util.Map<String, Object> ds1Row = new java.util.LinkedHashMap<>();
        ds1Row.put("id", "K1");
        ds1Row.put("name", "Alice");
        when(duckDbService.queryParquet(missingFromDs2, 0, 50)).thenReturn(List.of(ds1Row));

        var ds1Result = comparisonService.getMissing(id, 0, 50, "ds1");
        assertThat(ds1Result.totalElements()).isEqualTo(1L);
        assertThat(ds1Result.content().getFirst().missingFrom()).isEqualTo("DS2");
        assertThat(ds1Result.content().getFirst().direction()).isEqualTo("DS1");
        assertThat(ds1Result.content().getFirst().keyValues()).containsEntry("id", "K1");

        // direction ds2 -> missing_from_ds1.parquet
        when(duckDbService.countParquet(missingFromDs1)).thenReturn(2L);
        java.util.Map<String, Object> ds2Row = new java.util.LinkedHashMap<>();
        ds2Row.put("id", "K2");
        ds2Row.put("name", "Bob");
        when(duckDbService.queryParquet(missingFromDs1, 0, 50)).thenReturn(List.of(ds2Row));

        var ds2Result = comparisonService.getMissing(id, 0, 50, "ds2");
        assertThat(ds2Result.totalElements()).isEqualTo(2L);
        assertThat(ds2Result.content().getFirst().missingFrom()).isEqualTo("DS1");
        assertThat(ds2Result.content().getFirst().direction()).isEqualTo("DS2");
    }

    @Test
    @DisplayName("Should paginate matching records")
    void shouldPaginateMatchingRecords() throws Exception {
        String id = "completed-comp-matches";
        Path compDir = tempDir.resolve(id);
        Files.createDirectories(compDir);
        Path matchesPath = compDir.resolve("matches.parquet");
        Files.createFile(matchesPath);

        ComparisonRecord record = new ComparisonRecord();
        record.setId(id);
        record.setStatus(ComparisonStatus.COMPLETED);
        when(comparisonRepository.findById(id)).thenReturn(Optional.of(record));

        when(duckDbService.countParquet(matchesPath)).thenReturn(3L);
        java.util.Map<String, Object> matchRow = new java.util.LinkedHashMap<>();
        matchRow.put("_row_id", 1L);
        matchRow.put("id", 100);
        matchRow.put("name", "Same");
        when(duckDbService.queryParquet(matchesPath, "_row_id", 0, 50)).thenReturn(List.of(matchRow));

        var result = comparisonService.getMatches(id, 0, 50);
        assertThat(result.totalElements()).isEqualTo(3L);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst()).doesNotContainKey("_row_id");
        assertThat(result.content().getFirst()).containsEntry("id", 100);
        assertThat(result.content().getFirst()).containsEntry("name", "Same");
    }
}
