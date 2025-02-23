package com.lily.metadataProcessingService.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "metadata.processing")
@Data
public class ProcessingConfig {
    private int maxRetries = 3;
    private int batchSize = 100;
    private long retryDelayMs = 1000;
    private Map<String, String> rules = new HashMap<>();
    
    @Data
    public static class ProcessingRuleConfig {
        private List<String> allowedTypes;
        private Map<String, String> validationRules;
        private boolean requiresEncryption;
    }
} 