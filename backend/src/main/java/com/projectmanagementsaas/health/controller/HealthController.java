package com.projectmanagementsaas.health.controller;

import com.projectmanagementsaas.health.dto.HealthResponse;
import com.projectmanagementsaas.health.service.HealthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    ResponseEntity<HealthResponse> health() {
        HealthResponse response = healthService.health();
        return ResponseEntity.status("UP".equals(response.status()) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    @GetMapping("/ready")
    ResponseEntity<HealthResponse> ready() {
        return health();
    }
}
