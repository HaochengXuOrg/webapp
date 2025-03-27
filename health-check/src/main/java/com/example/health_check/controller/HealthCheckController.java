package com.example.health_check.controller;

import com.example.health_check.service.HealthCheckService;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);
    private final StatsDClient statsd;
    private final HealthCheckService healthCheckService;

    public HealthCheckController(HealthCheckService healthCheckService, StatsDClient statsd) {
        this.healthCheckService = healthCheckService;
        this.statsd = statsd;
    }

    @GetMapping
    public ResponseEntity<Void> checkHealth(HttpServletRequest request) throws IOException {

        long startTime = System.currentTimeMillis();
        statsd.incrementCounter("api.healthz.hitCount");
        logger.info("GET /healthz - checking service health.");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("X-Content-Type-Options", "nosniff");

        ResponseEntity<Void> response;

        if (!request.getParameterMap().isEmpty()) {
            logger.warn("GET /healthz - request has unexpected query parameters, returning 400");
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).build();
        }
        else if (request.getContentLengthLong() > 0) {
            logger.warn("GET /healthz - request has unexpected body content, returning 400");
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).build();
        }
        else if (healthCheckService.recordHealthCheck()) {
            logger.info("GET /healthz - service is healthy, returning 200");
            response = ResponseEntity.status(HttpStatus.OK).headers(headers).build();
        } else {
            logger.warn("GET /healthz - service not healthy, returning 503");
            response = ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).headers(headers).build();
        }
        long endTime = System.currentTimeMillis();
        statsd.recordExecutionTime("api.healthz.latency", endTime - startTime);

        return response;
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<String> methodNotAllowed() {

        logger.warn("Method not allowed on /healthz endpoint");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("X-Content-Type-Options", "nosniff");

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(headers)
                .build();
    }
}
