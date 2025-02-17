package com.lily.metadataProcessingService.config;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProcessingRules {
    private static final Map<String, String> rules = new ConcurrentHashMap<>();
    private static final String defaultRule = "";

    public String fetchRule(String rule, String fetchMethod) {
        if (fetchMethod.equals("EAGER")) {
            updateRules();
        }
        return rules.getOrDefault(rule, defaultRule);
    }

    public String fetchRule(String rule) {
        return rules.getOrDefault(rule, defaultRule);
    }

    public void updateRules() {
        // make api call for actual rules
        rules.put("slack", "doSomething");
    }


}
