package com.ecommerce.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private final JwtTokenService jwtTokenService = new JwtTokenService("bXlzZWNyZXRrZXltdXN0YmVleGFjdGx5MzJieXRlc2xvbmc=");

    @Test
    void generateAndValidateToken_shouldSucceed() {
        String token = jwtTokenService.generateToken(1, 1, "test@example.com");
        assertThat(token).isNotBlank();
        assertThat(jwtTokenService.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        assertThat(jwtTokenService.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void validateToken_withTamperedToken_shouldReturnFalse() {
        String token = jwtTokenService.generateToken(1, 1, "a@b.com");
        String tampered = token.substring(0, token.length() - 4) + "XXXX";
        assertThat(jwtTokenService.validateToken(tampered)).isFalse();
    }
}
