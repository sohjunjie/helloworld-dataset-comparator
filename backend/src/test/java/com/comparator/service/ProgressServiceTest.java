package com.comparator.service;

import com.comparator.config.AppProperties;
import com.comparator.model.dto.ProgressUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;

class ProgressServiceTest {

    private AppProperties appProperties;
    private ProgressService progressService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties(
                new AppProperties.StorageProperties("./data"),
                new AppProperties.UploadProperties("500MB"),
                new AppProperties.CleanupProperties(1),
                new AppProperties.ComparisonProperties(30)
        );
        progressService = new ProgressService(appProperties);
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should format ProgressUpdate as JSON: { \"stage\": \"COMPARING\", \"percent\": 75 }")
    void testProgressUpdateJsonFormat() throws Exception {
        ProgressUpdate update = new ProgressUpdate("COMPARING", 75);
        String json = objectMapper.writeValueAsString(update);
        assertThat(json).contains("\"stage\":\"COMPARING\"");
        assertThat(json).contains("\"percent\":75");
        assertThat(json).doesNotContain("message");

        ProgressUpdate failedUpdate = new ProgressUpdate("FAILED", 100, "Connection refused");
        String failedJson = objectMapper.writeValueAsString(failedUpdate);
        assertThat(failedJson).contains("\"stage\":\"FAILED\"");
        assertThat(failedJson).contains("\"percent\":100");
        assertThat(failedJson).contains("\"message\":\"Connection refused\"");
    }

    @Test
    @DisplayName("Should create SseEmitter with configured timeout of 30 minutes (1,800,000 ms)")
    void testSubscribeSetsTimeoutFromConfig() {
        String comparisonId = "test-comp-1";
        SseEmitter emitter = progressService.subscribe(comparisonId);

        assertThat(emitter).isNotNull();
        assertThat(emitter.getTimeout()).isEqualTo(30 * 60 * 1000L);
        assertThat(progressService.getEmitterCount(comparisonId)).isEqualTo(1);
    }

    @Test
    @DisplayName("Should allow multiple clients to subscribe to the same comparison ID")
    void testMultipleSubscribersForSameComparison() {
        String comparisonId = "multi-comp-1";
        SseEmitter emitter1 = progressService.subscribe(comparisonId);
        SseEmitter emitter2 = progressService.subscribe(comparisonId);

        assertThat(emitter1).isNotNull();
        assertThat(emitter2).isNotNull();
        assertThat(progressService.getEmitterCount(comparisonId)).isEqualTo(2);
    }

    @Test
    @DisplayName("Should emit progress events to all subscribers of a comparison")
    void testEmitProgressEvent() {
        String comparisonId = "comp-events-1";
        progressService.subscribe(comparisonId);
        progressService.subscribe(comparisonId);

        progressService.emit(comparisonId, "UPLOADING", 10);
        progressService.emit(comparisonId, "CONVERTING", 50);
        progressService.emit(comparisonId, "COMPARING", 75);

        assertThat(progressService.getEmitterCount(comparisonId)).isEqualTo(2);
    }

    @Test
    @DisplayName("Should clean up emitters and complete them when COMPLETED stage is emitted")
    void testEmitCompletedCleansUpEmitters() {
        String comparisonId = "comp-complete-1";
        progressService.subscribe(comparisonId);

        progressService.emit(comparisonId, "COMPLETED", 100);

        assertThat(progressService.getEmitterCount(comparisonId)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should clean up emitters when FAILED stage is emitted with error message")
    void testEmitFailedCleansUpEmitters() {
        String comparisonId = "comp-failed-1";
        progressService.subscribe(comparisonId);

        progressService.emit(comparisonId, "FAILED", 100, "Syntax error in SQL query");

        assertThat(progressService.getEmitterCount(comparisonId)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle emit to non-existent or empty subscriber list without error")
    void testEmitWithNoSubscribers() {
        progressService.emit("non-existent-id", "COMPARING", 50);
        progressService.emit("non-existent-id", new ProgressUpdate("COMPLETED", 100, null));
        assertThat(progressService.getEmitterCount("non-existent-id")).isEqualTo(0);
    }

    @Test
    @DisplayName("Should emit directly to single emitter via emitToEmitter")
    void testEmitToSingleEmitter() {
        SseEmitter emitter = new SseEmitter(10000L);
        progressService.emitToEmitter(emitter, new ProgressUpdate("COMPLETED", 100, null));
    }
}
