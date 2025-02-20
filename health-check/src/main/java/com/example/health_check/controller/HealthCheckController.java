package com.example.health_check.controller;

import com.example.health_check.service.HealthCheckService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

@RestController
@RequestMapping("/healthz")
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    public HealthCheckController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping
    public ResponseEntity<Void> checkHealth(HttpServletRequest request) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("X-Content-Type-Options", "nosniff");

        if (!request.getParameterMap().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .headers(headers)
                    .build();
        }

        if (request.getContentLengthLong() > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .headers(headers)
                    .build();
        }

        if (healthCheckService.recordHealthCheck()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .headers(headers)
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .headers(headers)
                    .build();
        }
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<String> methodNotAllowed() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("X-Content-Type-Options", "nosniff");

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(headers)
                .build();
    }
}
