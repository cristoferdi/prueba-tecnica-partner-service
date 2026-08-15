package com.telco.backend.security.jwt;

import com.telco.backend.config.security.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtProperties jwtProperties;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-for-jwt-unit-tests-only-32bytes");
        jwtProperties.setExpirationMs(3600000); // 1 hour

        jwtService = new JwtService(jwtProperties);
        signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void test_generacionToken() {
        String token = jwtService.generateToken("agente1", "AGENTE");
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void test_subjectDelToken() {
        String token = jwtService.generateToken("agente1", "AGENTE");
        String username = jwtService.getUsernameFromToken(token);
        assertThat(username).isEqualTo("agente1");
    }

    @Test
    void test_roleClaim() {
        String token = jwtService.generateToken("agente1", "AGENTE");
        String role = jwtService.getRoleFromToken(token);
        assertThat(role).isEqualTo("AGENTE");
    }

    @Test
    void test_issuedAt() {
        String token = jwtService.generateToken("agente1", "AGENTE");
        Claims claims = jwtService.getClaims(token);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
    }

    @Test
    void test_expiration() {
        String token = jwtService.generateToken("agente1", "AGENTE");
        Claims claims = jwtService.getClaims(token);
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration().getTime()).isGreaterThan(System.currentTimeMillis());
    }

    @Test
    void test_validacionTokenValido() {
        String token = jwtService.generateToken("agente1", "AGENTE");
        boolean isValid = jwtService.validateToken(token);
        assertThat(isValid).isTrue();
    }

    @Test
    void test_tokenExpirado() {
        String token = generateTokenWithExpiration("agente1", "AGENTE", -1000);
        boolean isValid = jwtService.validateToken(token);
        assertThat(isValid).isFalse();
    }

    @Test
    void test_tokenFirmadoConClaveIncorrecta() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("wrong-key-for-testing-purposes-32bytes!".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("agente1")
                .claim("role", "AGENTE")
                .signWith(wrongKey)
                .compact();
        boolean isValid = jwtService.validateToken(token);
        assertThat(isValid).isFalse();
    }

    private String generateTokenWithExpiration(String username, String role, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }
}