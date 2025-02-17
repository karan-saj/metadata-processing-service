package com.lily.metadataProcessingService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetadataRequest {
    private String eventType;
    private String eventId;
    private String timestamp;
    private Map<String, Object> payload;
    private Object metadata;
}
