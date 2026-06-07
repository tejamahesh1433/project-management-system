package com.projectmanagementsaas.security;

import com.projectmanagementsaas.common.exception.UnauthorizedException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BruteForceProtectionService {
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration lockoutDuration;

    public BruteForceProtectionService(
            @Value("${security.brute-force.max-attempts:5}") int maxAttempts,
            @Value("${security.brute-force.lockout-minutes:15}") long lockoutMinutes
    ) {
        this.maxAttempts = maxAttempts;
        this.lockoutDuration = Duration.ofMinutes(lockoutMinutes);
    }

    public void assertAllowed(String key) {
        Attempt attempt = attempts.get(normalize(key));
        if (attempt != null && attempt.lockedUntil() != null && attempt.lockedUntil().isAfter(Instant.now())) {
            throw new UnauthorizedException("Too many failed login attempts. Try again later");
        }
    }

    public void recordFailure(String key) {
        attempts.compute(normalize(key), (ignored, attempt) -> {
            int failures = attempt == null || expired(attempt) ? 1 : attempt.failures() + 1;
            Instant lockedUntil = failures >= maxAttempts ? Instant.now().plus(lockoutDuration) : null;
            return new Attempt(failures, lockedUntil, Instant.now());
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(normalize(key));
    }

    private boolean expired(Attempt attempt) {
        return attempt.lastAttemptAt().plus(lockoutDuration).isBefore(Instant.now());
    }

    private String normalize(String key) {
        return key == null ? "unknown" : key.trim().toLowerCase();
    }

    private record Attempt(int failures, Instant lockedUntil, Instant lastAttemptAt) {
    }
}
