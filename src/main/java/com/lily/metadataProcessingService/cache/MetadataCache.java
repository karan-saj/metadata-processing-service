package com.lily.metadataProcessingService.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MetadataCache {
    private final Cache<String, Map<String, String>> previousMetadataCache;
    
    public MetadataCache() {
        this.previousMetadataCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .build();
    }
    
    public Map<String, String> getPreviousMetadata(String key) {
        return previousMetadataCache.getIfPresent(key);
    }
    
    public void putMetadata(String key, Map<String, String> value) {
        previousMetadataCache.put(key, value);
    }
} 