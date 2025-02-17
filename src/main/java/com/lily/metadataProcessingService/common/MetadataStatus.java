package com.lily.metadataProcessingService.common;

public enum MetadataStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED;

    @Override
    public String toString() {
        return name().toLowerCase(); // Returns the status in lowercase
    }
}
