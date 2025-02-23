package com.lily.metadataProcessingService.util;

import java.util.Map;
import java.util.stream.Collectors;

public class CommonUtil {

    private CommonUtil() {
        // Private constructor to hide implicit public one
    }

    public static Map<String, String> convertToStringMap(Map<String, Object> objectMap) {
        return objectMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() != null ? entry.getValue().toString() : "null"
                ));
    }
}
