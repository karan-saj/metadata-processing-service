package com.lily.metadataProcessingService.rule;

import com.github.benmanes.caffeine.cache.Cache;
import com.lily.metadataProcessingService.context.TenantContext;
import com.lily.metadataProcessingService.exception.TenantAccessException;
import com.lily.metadataProcessingService.model.rules.MetadataRule;
import com.lily.metadataProcessingService.model.rules.ProcessingPriority;
import com.lily.metadataProcessingService.service.MetadataRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the rules for processing different types of metadata.
 * Think of this as our config which can be customized
 * Contains the rules for processing metadata, validations, transformations, etc.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessingRules {
    private static final Map<String, String> rules = new ConcurrentHashMap<>();
    private static final String defaultRule = "";

    private final Cache<String, MetadataRule> ruleCache;
    private final TenantContext tenantContext;
    private final MetadataRuleService ruleService;

    /**
     * Retrieves the processing rule for a given source ID and set the current tenant context.
     * Rules are cached and retrieved from the repository/api if not found in cache.
     *
     * @param sourceId The identifier of the source to get rules for
     * @return MetadataRule containing the processing configuration
     */
    public MetadataRule getRule(String sourceId) {
        String tenantId = tenantContext.getCurrentTenant();
        String cacheKey = tenantId + "_" + sourceId;
        
        return ruleCache.get(cacheKey, key -> {
            Optional<MetadataRule> tenantRule = ruleService
                .findByTenantIdAndSourceId(tenantId, sourceId);
            
            if (tenantRule.isPresent()) {
                MetadataRule rule = tenantRule.get();
                if (rule.isUseGlobalDefaults()) {
                    return mergeWithGlobalDefaults(rule);
                }
                return rule;
            }
            
            return getDefaultRule(tenantId);
        });
    }

    /**
     * Merges tenant-specific rule overrides with global default rules.
     * 
     * @param tenantRule The tenant-specific rule containing overrides
     * @return MetadataRule with merged configurations from global and tenant rules
     */
    private MetadataRule mergeWithGlobalDefaults(MetadataRule tenantRule) {
        MetadataRule globalRule = getGlobalRule(tenantRule.getSourceId());
        
        if (tenantRule.getConfiguration() != null) {
            applyOverrides(globalRule, tenantRule.getConfiguration());
        }
        
        return globalRule;
    }

    /**
     * Creates a default rule configuration for a given tenant.
     *
     * @param tenantId The tenant identifier to create default rules for
     * @return MetadataRule with default configuration values
     */
    private MetadataRule getDefaultRule(String tenantId) {
        return MetadataRule.builder()
            .id(tenantId + "_default")
            .tenantId(tenantId)
            .sourceId("default")
            .sourceType("INTERNAL")
            .allowedInputFormats("json")
            .allowedOutputFormats("json")
            .priority(ProcessingPriority.LOW)
            .batchingAllowed(true)
            .maxBatchSize(50)
            .build();
    }

    /**
     * Retrieves the global rule configuration for a given source.
     * Falls back to default global rule if none exists.
     *
     * @param sourceId The source identifier to get global rules for
     * @return MetadataRule containing the global configuration
     */
    private MetadataRule getGlobalRule(String sourceId) {
        return ruleService.findByTenantIdAndSourceId("global", sourceId)
            .orElseGet(() -> getDefaultRule("global"));
    }

    /**
     * Applies tenant-specific overrides to a rule configuration.
     *
     * @param rule The base rule to apply overrides to
     * @param overrides Map of configuration keys and values to override
     */
    private void applyOverrides(MetadataRule rule, Map<String, Object> overrides) {
        if (overrides.containsKey("priority")) {
            rule.setPriority(ProcessingPriority.valueOf(overrides.get("priority").toString()));
        }
        if (overrides.containsKey("maxBatchSize")) {
            rule.setMaxBatchSize((Integer) overrides.get("maxBatchSize"));
        }
    }

    /**
     * Validates that the current tenant has access to modify rules for the specified tenant.
     * Throws TenantAccessException if validation fails.
     *
     * @param tenantId The tenant ID being accessed
     * @param sourceId The source ID being accessed
     * @throws TenantAccessException if current tenant doesn't have access
     */
    public void validateTenantAccess(String tenantId, String sourceId) {
        String currentTenant = tenantContext.getCurrentTenant();
        if (!currentTenant.equals(tenantId)) {
            throw new TenantAccessException("Access denied to rule");
        }
    }
}