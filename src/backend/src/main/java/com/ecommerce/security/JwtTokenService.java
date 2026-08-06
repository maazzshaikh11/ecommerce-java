package com.ecommerce.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenService {

    private final SecretKey key;

    public JwtTokenService(@Value("${jwt.signing-key}") String signingKey) {
        byte[] keyBytes = Decoders.BASE64.decode(signingKey);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JwtSigningKey must be at least 32 bytes.");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(int userId, int customerId, String email) {
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("customer_id", String.valueOf(customerId))
            .claim("email", email)
            .id(UUID.randomUUID().toString())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600_000))
            .signWith(key)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
