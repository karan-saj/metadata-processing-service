package com.lily.metadataProcessingService.controller;

import com.lily.metadataProcessingService.dto.MetadataRequest;
import com.lily.metadataProcessingService.dto.MetadataStatusResponse;
import com.lily.metadataProcessingService.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/metadata")
@RequiredArgsConstructor
@Slf4j
public class MetadataController {

    private final IngestionService ingestionService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMetadata(@RequestBody MetadataRequest metadata) {
        log.info("Validating and ingesting metadata, requestId: {}", metadata.getEventId());
        ingestionService.ingestMetadata(metadata);
        log.info("Event process submitted successfully, requestId: {}", metadata.getEventId());
        return ResponseEntity.ok("Metadata published successfully!");
    }

    @GetMapping("/processing/{requestId}")
    public ResponseEntity<MetadataStatusResponse> getProcessingStatus(@RequestParam String requestId) {
        return ResponseEntity.ok(ingestionService.getStatusResponse(requestId));
    }
}
