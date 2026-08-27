package com.comparator.controller;

import com.comparator.model.dto.ComparisonRequest;
import com.comparator.model.dto.ComparisonSummary;
import com.comparator.model.entity.ComparisonRecord;
import com.comparator.model.enums.ComparisonStatus;
import com.comparator.repository.ComparisonRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/comparisons", "/api/comparisons"})
public class ComparisonController {

    private final ComparisonRepository comparisonRepository;

    public ComparisonController(ComparisonRepository comparisonRepository) {
        this.comparisonRepository = comparisonRepository;
    }

    @PostMapping
    public ResponseEntity<ComparisonSummary> createComparison(@Valid @RequestBody(required = false) ComparisonRequest request) {
        ComparisonRecord record = new ComparisonRecord();
        record.setId(UUID.randomUUID().toString());
        record.setStatus(ComparisonStatus.PENDING);
        record.setCreatedAt(LocalDateTime.now());

        if (request != null) {
            record.setDs1Type(request.ds1Type());
            record.setDs1FileName(request.ds1FileName());
            record.setDs2Type(request.ds2Type());
            record.setDs2FileName(request.ds2FileName());
        }

        ComparisonRecord saved = comparisonRepository.save(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(ComparisonSummary.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<ComparisonSummary>> listComparisons() {
        List<ComparisonRecord> records = comparisonRepository.findAllByOrderByCreatedAtDesc();
        List<ComparisonSummary> summaries = records.stream()
                .map(ComparisonSummary::fromEntity)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComparisonSummary> getComparisonById(@PathVariable String id) {
        ComparisonRecord record = comparisonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comparison not found with id: " + id));
        return ResponseEntity.ok(ComparisonSummary.fromEntity(record));
    }
}
