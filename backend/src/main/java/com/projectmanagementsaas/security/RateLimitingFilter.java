package com.projectmanagementsaas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final int requestsPerMinute;
    private final int authRequestsPerMinute;

    public RateLimitingFilter(
            @Value("${security.rate-limit.requests-per-minute:120}") int requestsPerMinute,
            @Value("${security.rate-limit.auth-requests-per-minute:20}") int authRequestsPerMinute
    ) {
        this.requestsPerMinute = requestsPerMinute;
        this.authRequestsPerMinute = authRequestsPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = clientIp(request) + ":" + bucket(request);
        int limit = request.getRequestURI().startsWith("/api/v1/auth") ? authRequestsPerMinute : requestsPerMinute;
        Window window = windows.compute(key, (ignored, current) -> nextWindow(current));
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - window.count())));
        if (window.count() > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Rate limit exceeded\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private Window nextWindow(Window current) {
        long now = Instant.now().getEpochSecond();
        long minute = now / 60;
        if (current == null || current.minute() != minute) {
            return new Window(minute, 1);
        }
        return new Window(minute, current.count() + 1);
    }

    private String bucket(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/v1/auth") ? "auth" : "api";
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("CF-Connecting-IP");
        if (forwarded == null || forwarded.isBlank()) {
            forwarded = request.getHeader("X-Forwarded-For");
        }
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record Window(long minute, int count) {
    }
}
