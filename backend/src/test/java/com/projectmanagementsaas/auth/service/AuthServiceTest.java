package com.projectmanagementsaas.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.projectmanagementsaas.auth.dto.ForgotPasswordRequest;
import com.projectmanagementsaas.auth.dto.RefreshTokenRequest;
import com.projectmanagementsaas.auth.dto.RegisterRequest;
import com.projectmanagementsaas.auth.dto.ResetPasswordRequest;
import com.projectmanagementsaas.auth.entity.PasswordResetToken;
import com.projectmanagementsaas.auth.entity.RefreshToken;
import com.projectmanagementsaas.auth.repository.PasswordResetTokenRepository;
import com.projectmanagementsaas.auth.repository.RefreshTokenRepository;
import com.projectmanagementsaas.auth.security.JwtService;
import com.projectmanagementsaas.auth.security.TokenBlacklistService;
import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.role.entity.Role;
import com.projectmanagementsaas.role.entity.RoleName;
import com.projectmanagementsaas.role.repository.RoleRepository;
import com.projectmanagementsaas.user.entity.User;
import com.projectmanagementsaas.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private PasswordEncoder passwordEncoder;
    private TokenHashService tokenHashService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        tokenHashService = new TokenHashService();
        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                passwordResetTokenRepository,
                roleRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                tokenHashService,
                tokenBlacklistService,
                30,
                30);
    }

    @Test
    void registerCreatesUserWithDefaultRoleAndTokens() {
        Role userRole = new Role();
        userRole.setName(RoleName.USER);

        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.createAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(900L);

        var response = authService.register(new RegisterRequest("User@Example.com", "password123", "User"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("user@example.com");
        assertThat(response.user().roles()).containsExactly(RoleName.USER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(passwordEncoder.matches("password123", userCaptor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("user@example.com", "password123", "User")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is already registered");
    }

    @Test
    void refreshRotatesRefreshToken() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setDisplayName("User");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHashService.hash("refresh-token"));
        refreshToken.setExpiresAt(Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByTokenHash(tokenHashService.hash("refresh-token")))
                .thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.createAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(900L);

        var response = authService.refresh(new RefreshTokenRequest("refresh-token"));

        assertThat(refreshToken.getRevokedAt()).isNotNull();
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isNotBlank();
    }

    @Test
    void forgotPasswordCreatesResetTokenForKnownUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setDisplayName("User");
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        var response = authService.forgotPassword(new ForgotPasswordRequest("user@example.com"));

        assertThat(response.resetToken()).isNotBlank();
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void resetPasswordChangesPasswordAndMarksTokenUsed() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash(passwordEncoder.encode("old-password"));

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(tokenHashService.hash("reset-token"));
        resetToken.setExpiresAt(Instant.now().plusSeconds(3600));

        when(passwordResetTokenRepository.findByTokenHash(tokenHashService.hash("reset-token")))
                .thenReturn(Optional.of(resetToken));

        authService.resetPassword(new ResetPasswordRequest("reset-token", "new-password"));

        assertThat(passwordEncoder.matches("new-password", user.getPasswordHash())).isTrue();
        assertThat(resetToken.getUsedAt()).isNotNull();
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).save(resetToken);
    }
}
