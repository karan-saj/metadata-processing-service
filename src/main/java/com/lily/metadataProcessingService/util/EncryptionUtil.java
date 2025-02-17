package com.lily.metadataProcessingService.util;

import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {
    public String maskSensitiveData(String data) {
        return data == null ? "****" : data.substring(0, 2) + "****" + data.substring(data.length() - 2);
    }
}

