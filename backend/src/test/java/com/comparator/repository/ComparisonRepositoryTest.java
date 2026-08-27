package com.comparator.repository;

import com.comparator.model.entity.ComparisonRecord;
import com.comparator.model.enums.ComparisonStatus;
import com.comparator.model.enums.DataSourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ComparisonRepositoryTest {

    @Autowired
    private ComparisonRepository comparisonRepository;

    @BeforeEach
    void setUp() {
        comparisonRepository.deleteAll();
    }

    @Test
    @DisplayName("findByCreatedAtBefore finds only records created before cutoff")
    void shouldFindRecordsCreatedBeforeCutoff() {
        LocalDateTime now = LocalDateTime.now();

        ComparisonRecord oldRecord = new ComparisonRecord();
        oldRecord.setId("old-id");
        oldRecord.setStatus(ComparisonStatus.COMPLETED);
        oldRecord.setCreatedAt(now.minusHours(2));
        oldRecord.setDs1Type(DataSourceType.FILE_UPLOAD);
        comparisonRepository.save(oldRecord);

        ComparisonRecord newRecord = new ComparisonRecord();
        newRecord.setId("new-id");
        newRecord.setStatus(ComparisonStatus.COMPLETED);
        newRecord.setCreatedAt(now.minusMinutes(10));
        newRecord.setDs1Type(DataSourceType.FILE_UPLOAD);
        comparisonRepository.save(newRecord);

        List<ComparisonRecord> expired = comparisonRepository.findByCreatedAtBefore(now.minusHours(1));
        assertThat(expired).hasSize(1);
        assertThat(expired.getFirst().getId()).isEqualTo("old-id");
    }

    @Test
    @DisplayName("findAllByOrderByCreatedAtDesc returns records in reverse chronological order")
    void shouldFindAllOrderedByCreatedAtDesc() {
        LocalDateTime now = LocalDateTime.now();

        ComparisonRecord rec1 = new ComparisonRecord();
        rec1.setId("id-1");
        rec1.setStatus(ComparisonStatus.PENDING);
        rec1.setCreatedAt(now.minusMinutes(30));
        comparisonRepository.save(rec1);

        ComparisonRecord rec2 = new ComparisonRecord();
        rec2.setId("id-2");
        rec2.setStatus(ComparisonStatus.PENDING);
        rec2.setCreatedAt(now.minusMinutes(10));
        comparisonRepository.save(rec2);

        List<ComparisonRecord> results = comparisonRepository.findAllByOrderByCreatedAtDesc();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo("id-2");
        assertThat(results.get(1).getId()).isEqualTo("id-1");
    }
}
