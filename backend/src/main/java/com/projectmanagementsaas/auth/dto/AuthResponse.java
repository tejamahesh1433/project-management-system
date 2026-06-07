package com.projectmanagementsaas.auth.dto;

public record AuthResponse(
        UserResponse user,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds
) {
}
