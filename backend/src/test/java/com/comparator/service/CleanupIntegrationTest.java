package com.comparator.service;

import com.comparator.config.AppProperties;
import com.comparator.model.entity.ComparisonRecord;
import com.comparator.model.enums.ComparisonStatus;
import com.comparator.model.enums.DataSourceType;
import com.comparator.repository.ComparisonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "app.cleanup.ttl-hours=1"
})
class CleanupIntegrationTest {

    @Autowired
    private CleanupService cleanupService;

    @Autowired
    private ComparisonRepository comparisonRepository;

    @Autowired
    private AppProperties appProperties;

    @Test
    @DisplayName("Should delete expired comparison database record and disk directory during cleanup")
    void shouldDeleteExpiredComparisonRecordAndFiles() throws IOException {
        String expiredId = UUID.randomUUID().toString();
        Path storageDir = Path.of(appProperties.storage().path(), expiredId);
        Files.createDirectories(storageDir);
        Path ds1Parquet = storageDir.resolve("ds1.parquet");
        Path ds2Parquet = storageDir.resolve("ds2.parquet");
        Files.writeString(ds1Parquet, "dummy ds1 parquet content");
        Files.writeString(ds2Parquet, "dummy ds2 parquet content");

        ComparisonRecord expiredRecord = new ComparisonRecord();
        expiredRecord.setId(expiredId);
        expiredRecord.setStatus(ComparisonStatus.COMPLETED);
        expiredRecord.setDs1Type(DataSourceType.FILE_UPLOAD);
        expiredRecord.setDs2Type(DataSourceType.FILE_UPLOAD);
        // Created 3 hours ago (TTL is 1 hour)
        expiredRecord.setCreatedAt(LocalDateTime.now().minusHours(3));
        comparisonRepository.save(expiredRecord);

        // Also create an active (unexpired) record to ensure it is NOT deleted
        String activeId = UUID.randomUUID().toString();
        Path activeStorageDir = Path.of(appProperties.storage().path(), activeId);
        Files.createDirectories(activeStorageDir);
        Path activeDs1Parquet = activeStorageDir.resolve("ds1.parquet");
        Files.writeString(activeDs1Parquet, "active ds1 parquet");

        ComparisonRecord activeRecord = new ComparisonRecord();
        activeRecord.setId(activeId);
        activeRecord.setStatus(ComparisonStatus.COMPLETED);
        activeRecord.setDs1Type(DataSourceType.FILE_UPLOAD);
        activeRecord.setDs2Type(DataSourceType.FILE_UPLOAD);
        activeRecord.setCreatedAt(LocalDateTime.now());
        comparisonRepository.save(activeRecord);

        // Trigger cleanup
        cleanupService.cleanup();

        // Expired record and directory should be deleted
        Optional<ComparisonRecord> deletedRecord = comparisonRepository.findById(expiredId);
        assertThat(deletedRecord).isEmpty();
        assertThat(Files.exists(storageDir)).isFalse();
        assertThat(Files.exists(ds1Parquet)).isFalse();

        // Active record and directory should still exist
        Optional<ComparisonRecord> remainingRecord = comparisonRepository.findById(activeId);
        assertThat(remainingRecord).isPresent();
        assertThat(Files.exists(activeStorageDir)).isTrue();
        assertThat(Files.exists(activeDs1Parquet)).isTrue();

        // Cleanup active test resources
        comparisonRepository.deleteById(activeId);
        org.springframework.util.FileSystemUtils.deleteRecursively(activeStorageDir);
    }

    @org.junit.jupiter.api.Nested
    @SpringBootTest
    @TestPropertySource(properties = {
            "app.cleanup.ttl-hours=0"
    })
    class ShortTtlOverrideTest {

        @Autowired
        private CleanupService cleanupService;

        @Autowired
        private ComparisonRepository comparisonRepository;

        @Autowired
        private AppProperties appProperties;

        @Test
        @DisplayName("Should delete record and files when short TTL (0 hours) is configured")
        void shouldDeleteRecordWithShortTtlOverride() throws IOException, InterruptedException {
            String id = UUID.randomUUID().toString();
            Path storageDir = Path.of(appProperties.storage().path(), id);
            Files.createDirectories(storageDir);
            Path ds1Parquet = storageDir.resolve("ds1.parquet");
            Files.writeString(ds1Parquet, "ds1 data");

            ComparisonRecord record = new ComparisonRecord();
            record.setId(id);
            record.setStatus(ComparisonStatus.COMPLETED);
            record.setCreatedAt(LocalDateTime.now().minusSeconds(2));
            comparisonRepository.save(record);

            cleanupService.cleanup();

            assertThat(comparisonRepository.findById(id)).isEmpty();
            assertThat(Files.exists(storageDir)).isFalse();
        }
    }
}
