package com.telco.backend.security;

import com.telco.backend.web.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
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

    private String generateTokenWithWrongKey(String username, String role) {
        SecretKey wrongKey = Keys.hmacShaKeyFor("this-is-a-wrong-secret-key-for-testing-purposes".getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(wrongKey)
                .compact();
    }

    private ResultActions login(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));
    }

    private ResultActions accessProtected(String token) throws Exception {
        return mockMvc.perform(get("/api/v1/test/protected")
                .header("Authorization", "Bearer " + token));
    }

    private ResultActions accessProtected() throws Exception {
        return mockMvc.perform(get("/api/v1/test/protected"));
    }

    // =========================================================================
    // Test 1: login correcto
    // =========================================================================
    @Test
    void test1_loginCorrecto() throws Exception {
        ResultActions result = login("agente1", "Agente*123");

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.role").value("AGENTE"));

        String responseBody = result.andReturn().getResponse().getContentAsString();
        Map<String, String> responseMap = objectMapper.readValue(responseBody, Map.class);
        assertThat(responseMap.get("token")).isNotEmpty();
    }

    // =========================================================================
    // Test 2: password incorrecta
    // =========================================================================
    @Test
    void test2_passwordIncorrecta() throws Exception {
        ResultActions result = login("agente1", "wrongpassword");

        result.andExpect(status().is(401))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    // =========================================================================
    // Test 3: usuario inexistente
    // =========================================================================
    @Test
    void test3_usuarioInexistente() throws Exception {
        ResultActions result = login("nonexistent", "password");

        result.andExpect(status().is(401))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    // =========================================================================
    // Test 4: usuario inactivo
    // =========================================================================
    @Test
    void test4_usuarioInactivo() throws Exception {
        ResultActions result = login("inactive1", "Agente*123");

        result.andExpect(status().is(401))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    // =========================================================================
    // Test 5: JWT válido - accede a recurso protegido
    // =========================================================================
    @Test
    void test5_jwtValidoAccedeRecursoProtegido() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        ResultActions result = accessProtected(token);

        result.andExpect(status().isOk());
    }

    // =========================================================================
    // Test 6: JWT expirado
    // =========================================================================
    @Test
    void test6_jwtExpirado() throws Exception {
        String token = generateTokenWithExpiration("agente1", "AGENTE", -1000);

        ResultActions result = accessProtected(token);

        result.andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Test 7: JWT manipulado (firma incorrecta)
    // =========================================================================
    @Test
    void test7_jwtManipulado() throws Exception {
        String token = generateTokenWithWrongKey("agente1", "AGENTE");

        ResultActions result = accessProtected(token);

        result.andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Test 7b: JWT manipulado (claim alterado)
    // =========================================================================
    @Test
    void test7b_jwtClaimManipulado() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        String[] parts = token.split("\\.");
        String manipulatedToken = parts[0] + "." + parts[1] + ".invalidsignature";

        ResultActions result = accessProtected(manipulatedToken);

        result.andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Test 8: JWT ausente
    // =========================================================================
    @Test
    void test8_jwtAusente() throws Exception {
        ResultActions result = accessProtected();

        result.andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Test 9: rol correcto en JWT
    // =========================================================================
    @Test
    void test9_rolCorrectoEnJWT() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        ResultActions result = mockMvc.perform(get("/api/v1/test/agente")
                .header("Authorization", "Bearer " + token));

        result.andExpect(status().isOk());
    }

    // =========================================================================
    // Test 9b: rol incorrecto - no puede acceder a recurso con hasRole
    // =========================================================================
    @Test
    void test9b_rolIncorrecto_noAccedeARolRestringido() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");

        ResultActions result = mockMvc.perform(get("/api/v1/test/agente")
                .header("Authorization", "Bearer " + token));

        result.andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 10: BCrypt verifica password de agente1
    // =========================================================================
    @Test
    void test10_bcryptVerificaPassword() {
        String storedHash = "$2b$10$IWDX2EfFKaFZwPkdzcSASuPmu/bmNSBlPDEOWPECTSbJPCI0og4Ci";

        boolean matches = passwordEncoder.matches("Agente*123", storedHash);

        assertThat(matches).isTrue();
    }

    // =========================================================================
    // Test 10b: BCrypt no verifica password incorrecta
    // =========================================================================
    @Test
    void test10b_bcryptNoVerificaPasswordIncorrecta() {
        String storedHash = "$2b$10$IWDX2EfFKaFZwPkdzcSASuPmu/bmNSBlPDEOWPECTSbJPCI0og4Ci";

        boolean matches = passwordEncoder.matches("wrongpassword", storedHash);

        assertThat(matches).isFalse();
    }

    // =========================================================================
    // Test extra: JWT contiene subject, role y expiración
    // =========================================================================
    @Test
    void testExtra_jwtContieneClaimsEsperados() {
        String token = generateToken("agente1", "AGENTE");

        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("agente1");
        assertThat(claims.get("role", String.class)).isEqualTo("AGENTE");
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration().getTime()).isGreaterThan(System.currentTimeMillis());
        assertThat(claims.getIssuedAt()).isNotNull();
    }

    // =========================================================================
    // Test extra: Login con validation - username vacío
    // =========================================================================
    @Test
    void testExtra_loginUsernameVacio() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("password");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(400));
    }

    // =========================================================================
    // Test extra: Login con validation - password vacío
    // =========================================================================
    @Test
    void testExtra_loginPasswordVacio() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("agente1");
        request.setPassword("");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(400));
    }

    // =========================================================================
    // Test extra: Login con validation - username null
    // =========================================================================
    @Test
    void testExtra_loginUsernameNull() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(null);
        request.setPassword("password");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(400));
    }

    // =========================================================================
    // Test: JWT válido pero username no existe en BD -> 401
    // =========================================================================
    @Test
    void test_jwtValidoUsuarioNoExiste_401() throws Exception {
        String token = generateToken("nonexistent_user", "AGENTE");

        ResultActions result = accessProtected(token);

        result.andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Test: usuario inactivo con JWT válido -> 401
    // =========================================================================
    @Test
    void test_usuarioInactivoConJwtValido_401() throws Exception {
        String token = generateToken("inactive1", "AGENTE");

        ResultActions result = accessProtected(token);

        result.andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Test: modificar claim role del JWT no eleva privilegios -> 403
    // =========================================================================
    @Test
    void test_manipulatedRoleClaim_noElevatesPrivileges_403() throws Exception {
        String token = generateTokenWithRole("back1", "AGENTE");

        ResultActions result = mockMvc.perform(get("/api/v1/test/agente")
                .header("Authorization", "Bearer " + token));

        result.andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test: JSON de error uniforme desde el entrypoint (401 sin token)
    // =========================================================================
    @Test
    void test_errorJsonUniforme_desdeEntryPoint() throws Exception {
        mockMvc.perform(get("/api/v1/ventas/mis-ventas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/ventas/mis-ventas"))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication failed"));
    }

    // =========================================================================
    // Test: JSON de error uniforme desde el denied handler (403)
    // =========================================================================
    @Test
    void test_errorJsonUniforme_desdeDeniedHandler() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        mockMvc.perform(get("/api/v1/reportes/resumen")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/reportes/resumen"))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    private String generateTokenWithRole(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }
}