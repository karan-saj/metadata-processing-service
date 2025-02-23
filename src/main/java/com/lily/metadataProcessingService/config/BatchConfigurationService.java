package com.lily.metadataProcessingService.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchConfigurationService {
    private final ApplicationProperties applicationProperties;
    
    public boolean isBatchEnabled(String topic) {
        return applicationProperties.getKafka().getBatch().getEnabledTopics().contains(topic);
    }
    
    public ApplicationProperties.TopicConfig getConfigForTopic(String topic) {
        return applicationProperties.getKafka().getBatch().getConfigurations().get(topic);
    }
}
