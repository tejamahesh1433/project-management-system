package com.projectmanagementsaas.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvValidator {
    private static final String DEFAULT_JWT_SECRET = "change-me-change-me-change-me-change-me-change-me-change-me";

    private final Environment environment;

    public EnvValidator(Environment environment) {
        this.environment = environment;
    }

    public ValidationResult validate() {
        List<String> errors = new ArrayList<>();
        require(errors, "spring.datasource.url");
        require(errors, "spring.datasource.username");
        require(errors, "spring.datasource.password");
        require(errors, "spring.data.redis.host");
        require(errors, "security.jwt.secret");

        String jwtSecret = environment.getProperty("security.jwt.secret");
        if (jwtSecret != null && (jwtSecret.length() < 48 || DEFAULT_JWT_SECRET.equals(jwtSecret))) {
            errors.add("security.jwt.secret must be changed and at least 48 characters");
        }
        return new ValidationResult(errors.isEmpty(), errors);
    }

    public boolean isProduction() {
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    private void require(List<String> errors, String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            errors.add(key + " is required");
        }
    }

    public record ValidationResult(boolean valid, List<String> errors) {
    }
}
