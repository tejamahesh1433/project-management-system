package com.projectmanagementsaas.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenHashServiceTest {
    private final TokenHashService tokenHashService = new TokenHashService();

    @Test
    void hashReturnsStableSha256Hex() {
        String first = tokenHashService.hash("token-value");
        String second = tokenHashService.hash("token-value");

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSize(64);
        assertThat(first).isNotEqualTo("token-value");
    }
}
