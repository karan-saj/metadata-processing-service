package com.lily.metadataProcessingService.model.rules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataRule {
    private String id;
    private String sourceId;
    private String sourceType;
    private String tenantId;
    private String allowedInputFormats;
    private String allowedOutputFormats;
    private String requiredFields;
    private String piiFields;
    private ProcessingPriority priority;
    private boolean batchingAllowed;
    private int maxBatchSize;
    private boolean useGlobalDefaults;
    private Map<String, Object> configuration;

    public List<String> getAllowedInputFormatsList() {
        return parseCommaSeparatedString(allowedInputFormats);
    }

    public List<String> getAllowedOutputFormatsList() {
        return parseCommaSeparatedString(allowedOutputFormats);
    }

    public List<String> getRequiredFieldsList() {
        return parseCommaSeparatedString(requiredFields);
    }

    public List<String> getPiiFieldsList() {
        return parseCommaSeparatedString(piiFields);
    }

    private List<String> parseCommaSeparatedString(String value) {
        return value != null ? Arrays.asList(value.split(",")) : new ArrayList<>();
    }

    private String convertListToString(List<String> list) {
        return list != null ? String.join(",", list) : "";
    }
}