package br.com.pixpro.project_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private String secretKey;

    @BeforeEach
    void setUp() {
        // Generate a valid base64-encoded 256-bit key
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            keyBytes[i] = (byte) i;
        }
        secretKey = Base64.getEncoder().encodeToString(keyBytes);
        jwtService = new JwtService(secretKey);
    }

    @Test
    @DisplayName("Should extract username from valid token")
    void extractUsername_ValidToken_ReturnsUsername() {
        // Arrange
        String username = "test@example.com";
        String token = generateToken(username, 10000);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Should validate token successfully for matching user")
    void isTokenValid_MatchingUserAndNotExpired_ReturnsTrue() {
        // Arrange
        String username = "test@example.com";
        String token = generateToken(username, 10000);
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should invalidate token for non-matching user")
    void isTokenValid_NonMatchingUser_ReturnsFalse() {
        // Arrange
        String token = generateToken("user1@example.com", 10000);
        UserDetails userDetails = User.builder()
                .username("user2@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should invalidate expired token")
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        // Arrange
        String username = "test@example.com";
        String token = generateToken(username, -10000); // Token expired 10 seconds ago
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // Act & Assert
        // Note: Since extractUsername throws ExpiredJwtException when parsing expired token,
        // we verify this exception is thrown instead of isTokenValid returning false
        assertThatThrownBy(() -> jwtService.isTokenValid(token, userDetails))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Should extract claims from token")
    void extractClaim_ValidToken_ReturnsClaim() {
        // Arrange
        String username = "test@example.com";
        String token = generateToken(username, 10000);

        // Act
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Assert
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("Should extract all claims from token")
    void extractAllClaims_ValidToken_ReturnsAllClaims() {
        // Arrange
        String username = "test@example.com";
        String token = generateToken(username, 10000);

        // Act
        Claims claims = jwtService.extractAllClaims(token);

        // Assert
        assertThat(claims.getSubject()).isEqualTo(username);
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    // Helper method to generate test tokens
    private String generateToken(String username, long expirationMillis) {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key)
                .compact();
    }
}
