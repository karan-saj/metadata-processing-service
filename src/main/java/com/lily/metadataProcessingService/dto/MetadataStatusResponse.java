package com.lily.metadataProcessingService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetadataStatusResponse {
    String requestId;
    String status;
    String message;
}
