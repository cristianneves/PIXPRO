package br.com.pixpro.notification_service.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    // 32-byte base64 key: "secretsecretsecretsecret"
    private static final String SECRET = "c2VjcmV0c2VjcmV0c2VjcmV0c2VjcmV0c2VjcmV0c2VjcmV0";

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    private String makeToken(String subject, Date issuedAt, Date expiration) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key())
                .compact();
    }

    @Test
    @DisplayName("isTokenValid true for non-expired token; extractUsername works")
    void tokenValidAndExtractUsername() {
        JwtService service = new JwtService(SECRET);
        String token = makeToken(
                "user@example.com",
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + 3_600_000)
        );

        assertTrue(service.isTokenValid(token));
        assertEquals("user@example.com", service.extractUsername(token));
    }

    @Test
    @DisplayName("isTokenValid false for expired token")
    void tokenInvalidWhenExpired() {
        JwtService service = new JwtService(SECRET);
        String token = makeToken(
                "user@example.com",
                new Date(System.currentTimeMillis() - 10_000),
                new Date(System.currentTimeMillis() - 5_000)
        );

        assertFalse(service.isTokenValid(token));
    }
}
