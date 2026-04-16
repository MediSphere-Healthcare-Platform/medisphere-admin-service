package com.medisphere.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "medisphere-notification-service", path = "/api/v1/notifications")
public interface NotificationClient {

    @PostMapping
    void createNotification(@RequestBody Map<String, Object> notificationRequest);
}
