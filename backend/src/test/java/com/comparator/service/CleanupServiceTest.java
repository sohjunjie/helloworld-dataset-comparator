package com.comparator.service;

import com.comparator.config.AppProperties;
import com.comparator.model.entity.ComparisonRecord;
import com.comparator.repository.ComparisonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CleanupServiceTest {

    @Mock
    private ComparisonRepository comparisonRepository;

    private AppProperties appProperties;
    private CleanupService cleanupService;

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
        cleanupService = new CleanupService(comparisonRepository, appProperties);
    }

    @Test
    @DisplayName("Should query expired records and delete both directory files and repository record")
    void shouldCleanUpExpiredRecordsAndFiles() throws IOException {
        String expiredId = "expired-id-123";
        Path compDir = tempDir.resolve(expiredId);
        Files.createDirectories(compDir);
        Path parquet1 = Files.createFile(compDir.resolve("ds1.parquet"));
        Path parquet2 = Files.createFile(compDir.resolve("ds2.parquet"));

        ComparisonRecord expiredRecord = new ComparisonRecord();
        expiredRecord.setId(expiredId);
        expiredRecord.setCreatedAt(LocalDateTime.now().minusHours(2));

        when(comparisonRepository.findByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(List.of(expiredRecord));

        cleanupService.cleanup();

        verify(comparisonRepository).findByCreatedAtBefore(any(LocalDateTime.class));
        verify(comparisonRepository).delete(expiredRecord);
        assertThat(Files.exists(compDir)).isFalse();
        assertThat(Files.exists(parquet1)).isFalse();
        assertThat(Files.exists(parquet2)).isFalse();
    }

    @Test
    @DisplayName("Should handle missing directory gracefully and still delete database record")
    void shouldHandleMissingDirectoryGracefully() {
        String expiredId = "non-existent-dir-id";
        Path compDir = tempDir.resolve(expiredId);
        assertThat(Files.exists(compDir)).isFalse();

        ComparisonRecord expiredRecord = new ComparisonRecord();
        expiredRecord.setId(expiredId);
        expiredRecord.setCreatedAt(LocalDateTime.now().minusHours(2));

        when(comparisonRepository.findByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(List.of(expiredRecord));

        cleanupService.cleanup();

        verify(comparisonRepository).delete(expiredRecord);
    }

    @Test
    @DisplayName("Should do nothing when no expired records are found")
    void shouldDoNothingWhenNoExpiredRecords() {
        when(comparisonRepository.findByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(List.of());

        cleanupService.cleanup();

        verify(comparisonRepository, never()).delete(any());
    }
}
