package com.example.health_check.service;

import com.example.health_check.model.HealthCheck;
import com.example.health_check.repository.HealthCheckRepository;
import org.springframework.stereotype.Service;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class HealthCheckService {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);
    private final StatsDClient statsd;

    private final HealthCheckRepository repository;

    public HealthCheckService(HealthCheckRepository repository, StatsDClient statsd) {
        this.repository = repository;
        this.statsd = statsd;
    }

    public boolean recordHealthCheck() {

        logger.info("Recording health check to DB.");

        try {
            long dbStart = System.currentTimeMillis();
            repository.save(new HealthCheck()); // Insert a new record
            long dbEnd = System.currentTimeMillis();

            statsd.recordExecutionTime("db.queryTime", dbEnd - dbStart);
            logger.info("Health check record inserted successfully.");
            return true;
        } catch (Exception e) {
            logger.error("Failed to record health check: ", e);
            return false; // Handle failure
        }
    }
}

