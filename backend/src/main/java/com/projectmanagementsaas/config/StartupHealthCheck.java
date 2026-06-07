package com.projectmanagementsaas.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupHealthCheck implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupHealthCheck.class);

    private final EnvValidator envValidator;

    public StartupHealthCheck(EnvValidator envValidator) {
        this.envValidator = envValidator;
    }

    @Override
    public void run(ApplicationArguments args) {
        EnvValidator.ValidationResult result = envValidator.validate();
        if (result.valid()) {
            log.info("Startup environment validation passed");
            return;
        }
        String message = "Startup environment validation failed: " + String.join("; ", result.errors());
        if (envValidator.isProduction()) {
            throw new IllegalStateException(message);
        }
        log.warn("{}; continuing because active profile is not production", message);
    }
}
