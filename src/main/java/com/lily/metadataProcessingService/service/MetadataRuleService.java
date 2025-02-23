package com.lily.metadataProcessingService.service;

import com.lily.metadataProcessingService.model.rules.MetadataRule;
import com.lily.metadataProcessingService.model.rules.ProcessingPriority;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class MetadataRuleService {
    
    public Optional<MetadataRule> findByTenantIdAndSourceId(String tenantId, String sourceId) {
        // Mocked API response
        return Optional.of(createMockRule(tenantId, sourceId));
    }
    
    private MetadataRule createMockRule(String tenantId, String sourceId) {
        Map<String, Object> config = new HashMap<>();
        config.put("validationLevel", "strict");
        config.put("enableEncryption", true);
        
        return MetadataRule.builder()
                .id(tenantId + "_" + sourceId)
                .tenantId(tenantId)
                .sourceId(sourceId)
                .sourceType("API")
                .allowedInputFormats("json,xml,csv")
                .allowedOutputFormats("json,avro")
                .requiredFields("id,name,timestamp")
                .piiFields("email,phone,ssn")
                .priority(ProcessingPriority.MEDIUM)
                .batchingAllowed(true)
                .maxBatchSize(1000)
                .useGlobalDefaults(false)
                .configuration(config)
                .build();
    }
} 