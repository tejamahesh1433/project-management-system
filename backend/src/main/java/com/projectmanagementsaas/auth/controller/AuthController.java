package com.projectmanagementsaas.auth.controller;

import com.projectmanagementsaas.auth.dto.AuthResponse;
import com.projectmanagementsaas.auth.dto.ForgotPasswordRequest;
import com.projectmanagementsaas.auth.dto.LoginRequest;
import com.projectmanagementsaas.auth.dto.MessageResponse;
import com.projectmanagementsaas.auth.dto.PasswordResetResponse;
import com.projectmanagementsaas.auth.dto.RefreshTokenRequest;
import com.projectmanagementsaas.auth.dto.RegisterRequest;
import com.projectmanagementsaas.auth.dto.ResetPasswordRequest;
import com.projectmanagementsaas.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    ResponseEntity<MessageResponse> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(authorization.substring(7), request);
        return ResponseEntity.ok(new MessageResponse("Logged out"));
    }

    @PostMapping("/forgot-password")
    ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("Password reset successful"));
    }
}
