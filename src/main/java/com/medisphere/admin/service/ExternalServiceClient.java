package com.medisphere.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ExternalServiceClient {

    private final RestTemplate restTemplate;

    public ExternalServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public void createAuthUser(String email, String password) {
        String url = "http://auth-service/auth/create-user";
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        payload.put("role", "DOCTOR");

        try {
            restTemplate.postForEntity(url, payload, String.class);
            log.info("Successfully created auth user for: {}", email);
        } catch (Exception e) {
            log.error("Failed to call Auth Service for: {}", email, e);
        }
    }

    public void sendNotification(String email, String message) {
        String url = "http://notification-service/notifications/send";
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("message", message);

        try {
            restTemplate.postForEntity(url, payload, String.class);
            log.info("Successfully sent notification to: {}", email);
        } catch (Exception e) {
            log.error("Failed to call Notification Service for: {}", email, e);
        }
    }
}
