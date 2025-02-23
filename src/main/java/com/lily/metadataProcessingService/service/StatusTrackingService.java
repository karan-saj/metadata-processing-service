package com.lily.metadataProcessingService.service;

import com.lily.metadataProcessingService.common.MetadataStatus;
import com.lily.metadataProcessingService.dto.MetadataStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class StatusTrackingService {
    private final Map<String, MetadataStatusResponse> taskStatus = new ConcurrentHashMap<>();
    
    public void updateStatus(String eventId, MetadataStatus status, String message) {
        taskStatus.put(eventId, new MetadataStatusResponse(eventId, status.toString(), message));
        log.info("Status updated for eventId: {} to: {}", eventId, status);
    }
    
    public MetadataStatusResponse getStatus(String eventId) {
        return taskStatus.get(eventId);
    }
} 