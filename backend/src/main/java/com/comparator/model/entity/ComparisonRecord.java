package com.comparator.model.entity;

import com.comparator.model.enums.ComparisonStatus;
import com.comparator.model.enums.DataSourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comparisons")
public class ComparisonRecord {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ComparisonStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Dataset 1 metadata
    @Enumerated(EnumType.STRING)
    @Column(name = "ds1_type", length = 32)
    private DataSourceType ds1Type;

    @Column(name = "ds1_file_name")
    private String ds1FileName;

    // Dataset 2 metadata
    @Enumerated(EnumType.STRING)
    @Column(name = "ds2_type", length = 32)
    private DataSourceType ds2Type;

    @Column(name = "ds2_file_name")
    private String ds2FileName;

    // Configuration (stored as JSON string)
    @Column(name = "config_json", length = 4000)
    private String configJson;

    // Summary results
    @Column(name = "ds1_record_count")
    private Long ds1RecordCount;

    @Column(name = "ds2_record_count")
    private Long ds2RecordCount;

    @Column(name = "ds1_fully_matching")
    private Long ds1FullyMatching;

    @Column(name = "ds2_fully_matching")
    private Long ds2FullyMatching;

    @Column(name = "ds1_not_matching")
    private Long ds1NotMatching;

    @Column(name = "ds2_not_matching")
    private Long ds2NotMatching;

    @Column(name = "ds1_missing_in_ds2")
    private Long ds1MissingInDs2;

    @Column(name = "ds2_missing_in_ds1")
    private Long ds2MissingInDs1;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    public ComparisonRecord() {
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ComparisonStatus.PENDING;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ComparisonStatus getStatus() {
        return status;
    }

    public void setStatus(ComparisonStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public DataSourceType getDs1Type() {
        return ds1Type;
    }

    public void setDs1Type(DataSourceType ds1Type) {
        this.ds1Type = ds1Type;
    }

    public String getDs1FileName() {
        return ds1FileName;
    }

    public void setDs1FileName(String ds1FileName) {
        this.ds1FileName = ds1FileName;
    }

    public DataSourceType getDs2Type() {
        return ds2Type;
    }

    public void setDs2Type(DataSourceType ds2Type) {
        this.ds2Type = ds2Type;
    }

    public String getDs2FileName() {
        return ds2FileName;
    }

    public void setDs2FileName(String ds2FileName) {
        this.ds2FileName = ds2FileName;
    }

    public String getConfigJson() {
        return configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public Long getDs1RecordCount() {
        return ds1RecordCount;
    }

    public void setDs1RecordCount(Long ds1RecordCount) {
        this.ds1RecordCount = ds1RecordCount;
    }

    public Long getDs2RecordCount() {
        return ds2RecordCount;
    }

    public void setDs2RecordCount(Long ds2RecordCount) {
        this.ds2RecordCount = ds2RecordCount;
    }

    public Long getDs1FullyMatching() {
        return ds1FullyMatching;
    }

    public void setDs1FullyMatching(Long ds1FullyMatching) {
        this.ds1FullyMatching = ds1FullyMatching;
    }

    public Long getDs2FullyMatching() {
        return ds2FullyMatching;
    }

    public void setDs2FullyMatching(Long ds2FullyMatching) {
        this.ds2FullyMatching = ds2FullyMatching;
    }

    public Long getDs1NotMatching() {
        return ds1NotMatching;
    }

    public void setDs1NotMatching(Long ds1NotMatching) {
        this.ds1NotMatching = ds1NotMatching;
    }

    public Long getDs2NotMatching() {
        return ds2NotMatching;
    }

    public void setDs2NotMatching(Long ds2NotMatching) {
        this.ds2NotMatching = ds2NotMatching;
    }

    public Long getDs1MissingInDs2() {
        return ds1MissingInDs2;
    }

    public void setDs1MissingInDs2(Long ds1MissingInDs2) {
        this.ds1MissingInDs2 = ds1MissingInDs2;
    }

    public Long getDs2MissingInDs1() {
        return ds2MissingInDs1;
    }

    public void setDs2MissingInDs1(Long ds2MissingInDs1) {
        this.ds2MissingInDs1 = ds2MissingInDs1;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
