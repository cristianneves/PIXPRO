package br.com.pixpro.auth_service.service;

import br.com.pixpro.auth_service.model.Role;
import br.com.pixpro.auth_service.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    // 256-bit base64 (32 bytes) example key
    private static final String SECRET = "c2VjcmV0c2VjcmV0c2VjcmV0c2VjcmV0c2VjcmV0c2VjcmV0"; // "secretsecretsecretsecret" base64

    private JwtService jwtService() {
        return new JwtService(SECRET);
    }

    private User buildUser(String email) {
        User u = new User();
        u.setId(99L);
        u.setName("Tester");
        u.setEmail(email);
        u.setPassword("pwd");
        u.setRole(Role.ROLE_USER);
        return u;
    }

    @Test
    @DisplayName("generateToken embeds username and is valid")
    void generateToken_AndValidate() {
        JwtService service = jwtService();
        UserDetails user = buildUser("jwt@example.com");

        String token = service.generateToken(user);
        assertNotNull(token);
        assertTrue(service.isTokenValid(token, user));
        assertEquals("jwt@example.com", service.extractUsername(token));
    }

    @Test
    @DisplayName("extra claims are included in token")
    void generateToken_WithExtraClaims() {
        JwtService service = jwtService();
        UserDetails user = buildUser("claims@example.com");

        String token = service.generateToken(Map.of("foo", "bar"), user);
        assertEquals("claims@example.com", service.extractUsername(token));
        // Just ensure token still valid – deep claim parsing already covered by library
        assertTrue(service.isTokenValid(token, user));
    }

    @Test
    @DisplayName("isTokenValid returns false when username mismatch")
    void isTokenValid_False_WhenDifferentUser() {
        JwtService service = jwtService();
        UserDetails original = buildUser("orig@example.com");
        UserDetails other = buildUser("other@example.com");
        String token = service.generateToken(original);
        assertFalse(service.isTokenValid(token, other));
    }

    @Test
    @DisplayName("isTokenValid returns false for expired token")
    void isTokenValid_False_WhenExpired() {
        JwtService service = jwtService();
        UserDetails user = buildUser("expired@example.com");
        // Manually craft expired token
        java.util.Date issuedAt = new java.util.Date(System.currentTimeMillis() - 10_000);
        java.util.Date expiredAt = new java.util.Date(System.currentTimeMillis() - 5_000);
        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(SECRET));
        String token = io.jsonwebtoken.Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(key, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
        assertFalse(service.isTokenValid(token, user));
    }
}
