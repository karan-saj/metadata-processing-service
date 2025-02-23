package com.lily.metadataProcessingService.controller;

import com.lily.metadataProcessingService.dto.MetadataRequest;
import com.lily.metadataProcessingService.dto.MetadataStatusResponse;
import com.lily.metadataProcessingService.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles all incoming HTTP requests for metadata processing.
 * This is where external systems send their metadata for processing.
 * Ideally, entry point will be Kafka consumer and this will called for manual processing
 */
@Slf4j
@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final IngestionService ingestionService;

    /**
     * Simple health check endpoint
     * @return pong response
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    /**
     * Entry point for metadata processing.
     * @return
     */
    @PostMapping("/process")
    public ResponseEntity<String> processMetadata(@RequestBody MetadataRequest request) {
        log.info("Received metadata request for processing. EventId: {}, Type: {}", 
            request.getEventId(), request.getEventType());
        
        try {
            ingestionService.ingestMetadata(request);
            log.info("Successfully queued metadata for processing. EventId: {}", 
                request.getEventId());
            return ResponseEntity.accepted().body("Processing started");
        } catch (Exception e) {
            log.error("Failed to process metadata request. EventId: {}, Error: {}", 
                request.getEventId(), e.getMessage());
            return ResponseEntity.internalServerError().body("Processing failed");
        }
    }

    /**
     * Get the status of a metadata request.
     * @return
     */
    @GetMapping("/status/{eventId}")
    public ResponseEntity<MetadataStatusResponse> getStatus(@PathVariable String eventId) {
        log.info("Checking status for eventId: {}", eventId);
        return ResponseEntity.ok(ingestionService.getStatusResponse(eventId));
    }
}
