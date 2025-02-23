package com.lily.metadataProcessingService.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom exception for metadata processing errors.
 */
@Slf4j
public class ProcessingException extends RuntimeException {

    public ProcessingException(String message) {
        super(message);
        log.error("Processing error: {}", message);
    }

    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
        log.error("Processing error: {}. Cause: {}", message, cause.getMessage());
    }
}

