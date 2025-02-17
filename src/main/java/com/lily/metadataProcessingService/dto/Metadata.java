package com.lily.metadataProcessingService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Metadata {
    private String eventType;
    private String metaDataType;
    private Map<String, Object> metaData;
}
