package com.example.health_check.service;

import com.example.health_check.model.HealthCheck;
import com.example.health_check.repository.HealthCheckRepository;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {

    private final HealthCheckRepository repository;

    public HealthCheckService(HealthCheckRepository repository) {
        this.repository = repository;
    }

    public boolean recordHealthCheck() {
        try {
            repository.save(new HealthCheck()); // Insert a new record
            return true;
        } catch (Exception e) {
            return false; // Handle failure
        }
    }
}

