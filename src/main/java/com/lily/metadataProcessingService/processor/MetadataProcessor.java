package com.lily.metadataProcessingService.processor;

import com.lily.metadataProcessingService.dto.Metadata;
import com.lily.metadataProcessingService.dto.MetadataRequest;
import com.lily.metadataProcessingService.model.rules.MetadataRule;

import java.util.Set;

public interface MetadataProcessor {
    Metadata process(MetadataRequest request, MetadataRule rule);
    Set<String> getSupportedTypes();
}