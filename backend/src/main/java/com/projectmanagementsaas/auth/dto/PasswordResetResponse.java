package com.projectmanagementsaas.auth.dto;

public record PasswordResetResponse(String message, String resetToken) {
}
