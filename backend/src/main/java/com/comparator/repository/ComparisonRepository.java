package com.comparator.repository;

import com.comparator.model.entity.ComparisonRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComparisonRepository extends JpaRepository<ComparisonRecord, String> {

    List<ComparisonRecord> findByCreatedAtBefore(LocalDateTime cutoff);

    List<ComparisonRecord> findAllByOrderByCreatedAtDesc();
}
