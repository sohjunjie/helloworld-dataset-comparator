package com.comparator.service;

import com.comparator.config.AppProperties;
import com.comparator.model.entity.ComparisonRecord;
import com.comparator.repository.ComparisonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CleanupService {

    private static final Logger log = LoggerFactory.getLogger(CleanupService.class);

    private final ComparisonRepository comparisonRepository;
    private final AppProperties appProperties;

    public CleanupService(ComparisonRepository comparisonRepository, AppProperties appProperties) {
        this.comparisonRepository = comparisonRepository;
        this.appProperties = appProperties;
    }

    @Scheduled(fixedRate = 900000)
    public void cleanup() {
        int ttlHours = appProperties.cleanup() != null ? appProperties.cleanup().ttlHours() : 1;
        LocalDateTime cutoff = LocalDateTime.now().minusHours(ttlHours);
        log.info("Starting TTL cleanup for comparisons created before {}", cutoff);

        List<ComparisonRecord> expiredRecords = comparisonRepository.findByCreatedAtBefore(cutoff);
        if (expiredRecords.isEmpty()) {
            log.debug("No expired comparisons found to clean up");
            return;
        }

        log.info("Found {} expired comparison(s) to clean up", expiredRecords.size());
        for (ComparisonRecord record : expiredRecords) {
            cleanRecord(record);
        }
    }

    private void cleanRecord(ComparisonRecord record) {
        String comparisonId = record.getId();
        if (comparisonId == null || comparisonId.isBlank()) {
            comparisonRepository.delete(record);
            return;
        }

        String storagePath = appProperties.storage() != null ? appProperties.storage().path() : "./data";
        Path compDir = Path.of(storagePath, comparisonId);

        try {
            if (Files.exists(compDir)) {
                FileSystemUtils.deleteRecursively(compDir);
                log.debug("Deleted storage directory for comparison {}: {}", comparisonId, compDir);
            } else {
                log.debug("Storage directory for comparison {} already absent: {}", comparisonId, compDir);
            }
        } catch (Exception e) {
            log.warn("Failed to delete storage directory for comparison {} at {}: {}", comparisonId, compDir, e.getMessage());
        }

        try {
            comparisonRepository.delete(record);
            log.info("Deleted expired comparison record: {}", comparisonId);
        } catch (Exception e) {
            log.error("Failed to delete comparison record from database for id {}: {}", comparisonId, e.getMessage(), e);
        }
    }
}
