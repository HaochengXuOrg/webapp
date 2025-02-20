package com.example.health_check.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_checks")
public class HealthCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "check_time", nullable = false)
    private LocalDateTime checkTime;

    public HealthCheck() {
        this.checkTime = LocalDateTime.now(); // Current UTC time
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCheckTime() {
        return checkTime;
    }
}
