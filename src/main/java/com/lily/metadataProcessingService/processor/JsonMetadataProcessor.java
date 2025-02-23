package com.lily.metadataProcessingService.processor;

import com.lily.metadataProcessingService.dto.Metadata;
import com.lily.metadataProcessingService.dto.MetadataRequest;
import com.lily.metadataProcessingService.model.rules.MetadataRule;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class JsonMetadataProcessor implements MetadataProcessor {
    
    @Override
    public Metadata process(MetadataRequest request, MetadataRule rule) {
        // JSON processing logic
        return null; // implement actual processing
    }

    @Override
    public Set<String> getSupportedTypes() {
        return Set.of("json", "application/json");
    }
}
