package com.lily.metadataProcessingService.processor;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProcessorRegistry {
    private final Map<String, MetadataProcessor> processors = new HashMap<>();
    private final List<MetadataProcessor> processorList;

    public ProcessorRegistry(List<MetadataProcessor> processorList) {
        this.processorList = processorList;
    }

    @PostConstruct
    public void initializeRegistry() {
        processorList.forEach(processor -> {
            // Assuming each processor implementation defines its supported types
            processor.getSupportedTypes().forEach(type -> 
                processors.put(type.toLowerCase(), processor));
        });
    }

    public MetadataProcessor getProcessor(String dataType) {
        MetadataProcessor processor = processors.get(dataType.toLowerCase());
        if (processor == null) {
            throw new RuntimeException("No processor found for type: " + dataType);
        }
        return processor;
    }
} 