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
        comparisonService = new ComparisonService(comparisonRepository, comparisonEngine, progressService, appProperties, objectMapper);
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
}
