package com.lily.metadataProcessingService.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {
    private KafkaConfig kafka;

    @Data
    public static class KafkaConfig {
        private BatchConfig batch;
    }

    @Data
    public static class BatchConfig {
        private List<String> enabledTopics;
        private Map<String, TopicConfig> configurations;
    }

    @Data
    public static class TopicConfig {
        private int batchSize;
        private int timeoutMs;
    }
} 