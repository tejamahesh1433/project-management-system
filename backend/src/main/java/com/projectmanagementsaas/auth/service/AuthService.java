package com.projectmanagementsaas.auth.service;

import com.projectmanagementsaas.auth.dto.AuthResponse;
import com.projectmanagementsaas.auth.dto.ForgotPasswordRequest;
import com.projectmanagementsaas.auth.dto.LoginRequest;
import com.projectmanagementsaas.auth.dto.PasswordResetResponse;
import com.projectmanagementsaas.auth.dto.RefreshTokenRequest;
import com.projectmanagementsaas.auth.dto.RegisterRequest;
import com.projectmanagementsaas.auth.dto.ResetPasswordRequest;
import com.projectmanagementsaas.auth.dto.UserResponse;
import com.projectmanagementsaas.auth.entity.PasswordResetToken;
import com.projectmanagementsaas.auth.entity.RefreshToken;
import com.projectmanagementsaas.auth.repository.PasswordResetTokenRepository;
import com.projectmanagementsaas.auth.repository.RefreshTokenRepository;
import com.projectmanagementsaas.auth.security.JwtService;
import com.projectmanagementsaas.auth.security.TokenBlacklistService;
import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.common.exception.UnauthorizedException;
import com.projectmanagementsaas.role.entity.RoleName;
import com.projectmanagementsaas.role.repository.RoleRepository;
import com.projectmanagementsaas.security.BruteForceProtectionService;
import com.projectmanagementsaas.security.PasswordPolicyValidator;
import com.projectmanagementsaas.user.entity.User;
import com.projectmanagementsaas.user.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenHashService tokenHashService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Duration refreshTokenTtl;
    private final Duration passwordResetTtl;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            TokenHashService tokenHashService,
            TokenBlacklistService tokenBlacklistService,
            PasswordPolicyValidator passwordPolicyValidator,
            BruteForceProtectionService bruteForceProtectionService,
            @Value("${security.jwt.refresh-token-ttl-days}") long refreshTokenTtlDays,
            @Value("${security.password-reset.token-ttl-minutes}") long passwordResetTtlMinutes
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenHashService = tokenHashService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.bruteForceProtectionService = bruteForceProtectionService;
        this.refreshTokenTtl = Duration.ofDays(refreshTokenTtlDays);
        this.passwordResetTtl = Duration.ofMinutes(passwordResetTtlMinutes);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        passwordPolicyValidator.validate(request.password());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email is already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.getRoles().add(roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException("Default USER role is missing")));

        return issueTokens(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        bruteForceProtectionService.assertAllowed(email);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (AuthenticationException exception) {
            bruteForceProtectionService.recordFailure(email);
            throw new UnauthorizedException("Invalid email or password");
        }

        bruteForceProtectionService.recordSuccess(email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = tokenHashService.hash(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.getRevokedAt() != null || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        refreshToken.revoke();
        return issueTokens(refreshToken.getUser());
    }

    public void logout(String accessToken, RefreshTokenRequest request) {
        tokenBlacklistService.blacklist(accessToken, Duration.ofSeconds(jwtService.accessTokenTtlSeconds()));
        refreshTokenRepository.findByTokenHash(tokenHashService.hash(request.refreshToken()))
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public PasswordResetResponse forgotPassword(ForgotPasswordRequest request) {
        return userRepository.findByEmailIgnoreCase(request.email())
                .map(user -> {
                    String rawToken = randomToken();
                    PasswordResetToken resetToken = new PasswordResetToken();
                    resetToken.setUser(user);
                    resetToken.setTokenHash(tokenHashService.hash(rawToken));
                    resetToken.setExpiresAt(Instant.now().plus(passwordResetTtl));
                    passwordResetTokenRepository.save(resetToken);
                    return new PasswordResetResponse("Password reset token generated", rawToken);
                })
                .orElseGet(() -> new PasswordResetResponse("If the email exists, a reset token was generated", null));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        passwordPolicyValidator.validate(request.newPassword());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHashService.hash(request.token()))
                .orElseThrow(() -> new BadRequestException("Invalid password reset token"));

        if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid password reset token");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        resetToken.markUsed();
        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);
    }

    private AuthResponse issueTokens(User user) {
        String refreshTokenValue = randomToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHashService.hash(refreshTokenValue));
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenTtl));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                toUserResponse(user),
                jwtService.createAccessToken(user),
                refreshTokenValue,
                "Bearer",
                jwtService.accessTokenTtlSeconds());
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRoles().stream().map(role -> role.getName()).collect(java.util.stream.Collectors.toSet()));
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
