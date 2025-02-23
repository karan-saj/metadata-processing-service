package com.lily.metadataProcessingService.model;

import lombok.Data;

import java.util.Map;

@Data
public class MetadataRequest {
    private String format;
    private String content;
    private Map<String, Object> metadata;
} 