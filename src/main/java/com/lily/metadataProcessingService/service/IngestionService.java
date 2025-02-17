package com.lily.metadataProcessingService.service;

import com.lily.metadataProcessingService.common.MetadataStatus;
import com.lily.metadataProcessingService.dto.MetadataRequest;
import com.lily.metadataProcessingService.dto.MetadataStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final Map<String, MetadataStatusResponse> taskStatus = new ConcurrentHashMap<>();
    private static final int MAX_RETRIES = 3;

    private final ProcessingService processingService;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public void ingestMetadata(MetadataRequest request) {
        log.info("Validating and ingesting metadata, requestId: {}", request.getEventId());
        taskStatus.put(request.getEventId(), new MetadataStatusResponse(request.getEventId(), MetadataStatus.PENDING.toString(), "File processing started."));
        executorService.submit(() -> processMetaData(request));
        log.info("Metadata successfully ingested, requestId: {}", request.getEventId());
    }

    private void processMetaData(MetadataRequest request) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                log.info("Metadata processing started :{} on attempt: {}", request.getEventId(), attempt);
                taskStatus.put(request.getEventId(), new MetadataStatusResponse(request.getEventId(), MetadataStatus.PROCESSING.toString(), "metadata processing started."));
                processingService.processMetadata(request);
                return;
            } catch (Exception e) {
                attempt++;
                log.warn("Metadata processing failed for :{} on attempt :{}", request.getEventId(), attempt);
                if (attempt == MAX_RETRIES) {
                    log.error("Metadata processing failed for :{}, all retries exhausted :{}", request.getEventId(), attempt);
                    taskStatus.put(request.getEventId(), new MetadataStatusResponse(request.getEventId(), MetadataStatus.FAILED.toString(), "Error processing metadata after retries: " + e.getMessage()));
                } else {
                    try {
                        Thread.sleep((long) Math.pow(2, attempt) * 1000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    public MetadataStatusResponse getStatusResponse(String requestId) {
        log.info("Fetching status for id, requestId: {}", requestId);
        return taskStatus.get(requestId);
    }
}
