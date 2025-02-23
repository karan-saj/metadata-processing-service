package com.lily.metadataProcessingService.service;

import com.lily.metadataProcessingService.common.MetadataStatus;
import com.lily.metadataProcessingService.dto.MetadataRequest;
import com.lily.metadataProcessingService.dto.MetadataStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles the initial step and queueing of metadata.
 * Think of this as the main entry point for metadata processing.
 * and makes sure it gets to the right place for processing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final ProcessingService processingService;
    private final StatusTrackingService statusTrackingService;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public void ingestMetadata(MetadataRequest request) {
        log.info("Starting metadata ingestion for eventId: {}", request.getEventId());
        
        statusTrackingService.updateStatus(request.getEventId(), 
            MetadataStatus.PENDING, "Started processing");
        
        try {
            log.debug("Queueing metadata for processing. EventId: {}", request.getEventId());
            executorService.submit(() -> processingService.processMetadata(request));
            log.info("Successfully queued metadata. EventId: {}", request.getEventId());
        } catch (Exception e) {
            log.error("Failed to ingest metadata. EventId: {}, Error: {}", 
                request.getEventId(), e.getMessage(), e);
            statusTrackingService.updateStatus(request.getEventId(), 
                MetadataStatus.FAILED, e.getMessage());
            throw e;
        }
    }

    public MetadataStatusResponse getStatusResponse(String requestId) {
        log.info("Fetching status for id, requestId: {}", requestId);
        return statusTrackingService.getStatus(requestId);
    }
}
