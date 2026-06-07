package com.projectmanagementsaas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InputSanitizationFilter extends OncePerRequestFilter {
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]");
    private static final Pattern SCRIPT_TAG = Pattern.compile("(?i)<\\s*/?\\s*script\\b");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String query = request.getQueryString();
        if (unsafe(query) || request.getHeaderNames().asIterator().hasNext() && unsafeHeaders(request)) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Unsafe request input\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean unsafeHeaders(HttpServletRequest request) {
        return request.getHeaderNames().asIterator()
                .hasNext()
                && java.util.Collections.list(request.getHeaderNames()).stream()
                .map(request::getHeader)
                .anyMatch(this::unsafe);
    }

    private boolean unsafe(String value) {
        return value != null && (CONTROL_CHARS.matcher(value).find() || SCRIPT_TAG.matcher(value).find());
    }
}
