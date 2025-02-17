package com.lily.metadataProcessingService.util;

import com.lily.metadataProcessingService.dto.MetadataRequest;

public class DataValidationUtil {
    public static boolean isValid(MetadataRequest request) {
        return request != null && request.getPayload() != null && request.getMetadata() != null;
    }
}

