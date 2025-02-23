package com.lily.metadataProcessingService.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class OAuthTokenValidator {

    private static final String OAUTH_SERVER_URL = "https://auth.example.com/introspect";

    public boolean validateToken(String token) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.postForEntity(OAUTH_SERVER_URL, entity, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("OAuth token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean hasPermission(String token, String eventType) {
        // compare against rule to check if token as required permission for event type
        return true;
    }
}

