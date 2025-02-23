package com.lily.metadataProcessingService.context;

import org.springframework.stereotype.Component;

/**
 * Manages tenant context for multi-tenancy support.
 */
@Component
public class TenantContext {
    private final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    /**
     * Sets the current tenant ID for the executing thread.
     * @param tenantId The ID of the tenant to set as current
     */
    public void setCurrentTenant(String tenantId) {
        currentTenant.set(tenantId);
    }

    /**
     * Retrieves the current tenant ID for the executing thread.
     * @return The current tenant ID, or null if not set
     */
    public String getCurrentTenant() {
        return currentTenant.get();
    }

    /**
     * Clears the tenant context for the current thread.
     * Should be called after request processing is complete.
     */
    public void clear() {
        currentTenant.remove();
    }
} 