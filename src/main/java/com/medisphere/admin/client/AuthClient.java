package com.medisphere.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "medisphere-auth-service", path = "/api/v1/auth")
public interface AuthClient {

    @PostMapping("/create-user")
    Map<String, Object> createAuthUser(@RequestBody Map<String, String> payload);

    @DeleteMapping("/user")
    void deleteAuthUser(@RequestParam("email") String email);
}
