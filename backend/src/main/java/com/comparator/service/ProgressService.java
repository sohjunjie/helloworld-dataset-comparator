package com.comparator.service;

import com.comparator.config.AppProperties;
import com.comparator.model.dto.ProgressUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ProgressService {

    private static final Logger log = LoggerFactory.getLogger(ProgressService.class);
    private static final long DEFAULT_TIMEOUT_MS = 30 * 60 * 1000L; // 30 minutes

    private final AppProperties appProperties;
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public ProgressService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public SseEmitter subscribe(String comparisonId) {
        long timeoutMs = resolveTimeoutMs();
        SseEmitter emitter = new SseEmitter(timeoutMs);

        emitters.computeIfAbsent(comparisonId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE emitter completed for comparison: {}", comparisonId);
            removeEmitter(comparisonId, emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE emitter timed out for comparison: {}", comparisonId);
            emitter.complete();
            removeEmitter(comparisonId, emitter);
        });

        emitter.onError(e -> {
            log.debug("SSE emitter error for comparison {}: {}", comparisonId, e.getMessage());
            removeEmitter(comparisonId, emitter);
        });

        return emitter;
    }

    public void emit(String comparisonId, String stage, int percent) {
        emit(comparisonId, new ProgressUpdate(stage, percent, null));
    }

    public void emit(String comparisonId, String stage, int percent, String message) {
        emit(comparisonId, new ProgressUpdate(stage, percent, message));
    }

    public void emit(String comparisonId, ProgressUpdate update) {
        if (comparisonId == null || update == null) {
            return;
        }

        List<SseEmitter> list = emitters.get(comparisonId);
        if (list == null || list.isEmpty()) {
            return;
        }

        boolean isTerminal = "COMPLETED".equalsIgnoreCase(update.stage()) || "FAILED".equalsIgnoreCase(update.stage());
        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(update, MediaType.APPLICATION_JSON));

                if (isTerminal) {
                    emitter.complete();
                    deadEmitters.add(emitter);
                }
            } catch (Exception e) {
                log.debug("Failed to send SSE event to emitter for comparison {}: {}", comparisonId, e.getMessage());
                deadEmitters.add(emitter);
            }
        }

        for (SseEmitter dead : deadEmitters) {
            removeEmitter(comparisonId, dead);
        }
    }

    public void emitToEmitter(SseEmitter emitter, ProgressUpdate update) {
        if (emitter == null || update == null) {
            return;
        }
        boolean isTerminal = "COMPLETED".equalsIgnoreCase(update.stage()) || "FAILED".equalsIgnoreCase(update.stage());
        try {
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(update, MediaType.APPLICATION_JSON));
            if (isTerminal) {
                emitter.complete();
            }
        } catch (Exception e) {
            log.debug("Failed to send direct SSE event to emitter: {}", e.getMessage());
            emitter.completeWithError(e);
        }
    }

    public int getEmitterCount(String comparisonId) {
        List<SseEmitter> list = emitters.get(comparisonId);
        return list == null ? 0 : list.size();
    }

    private void removeEmitter(String comparisonId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(comparisonId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(comparisonId);
            }
        }
    }

    private long resolveTimeoutMs() {
        if (appProperties != null && appProperties.comparison() != null && appProperties.comparison().timeoutMinutes() > 0) {
            return (long) appProperties.comparison().timeoutMinutes() * 60 * 1000L;
        }
        return DEFAULT_TIMEOUT_MS;
    }
}
