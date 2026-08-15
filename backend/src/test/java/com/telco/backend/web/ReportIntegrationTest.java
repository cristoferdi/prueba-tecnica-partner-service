package com.telco.backend.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
class ReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    // =========================================================================
    // Test 1: SUPERVISOR obtiene resumen -> 200
    // conteos: PENDIENTE=4, APROBADA=3, RECHAZADA=2
    // monto_total_aprobadas = 3500+1500+9000 = 14000.00
    // ventas_por_dia: 9 entries
    // =========================================================================
    @Test
    void test_resumen_exitoso() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/reportes/resumen")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteosPorEstado").isArray())
                .andExpect(jsonPath("$.conteosPorEstado.length()").value(3))
                .andExpect(jsonPath("$.montoTotalAprobadas").value(14000.00))
                .andExpect(jsonPath("$.ventasPorDia").isArray())
                .andExpect(jsonPath("$.ventasPorDia.length()").value(9));
    }

    // =========================================================================
    // Test 2: SUPERVISOR verifica conteos por estado - 3 estados presentes
    // =========================================================================
    @Test
    void test_resumen_conteosPorEstado() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/reportes/resumen")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteosPorEstado.length()").value(3))
                .andExpect(jsonPath("$.conteosPorEstado[*].estado").isNotEmpty())
                .andExpect(jsonPath("$.conteosPorEstado[*].count").isNotEmpty());
    }

    // =========================================================================
    // Test 3: SUPERVISOR con rango de fechas -> ventas_por_dia filtradas
    // desde=2025-01-10T00:00:00Z, hasta=2025-01-14T23:59:59Z
    // ventas in range: 2(01-10), 5(01-08 - no, before), 3(01-12), 6(01-14)
    // Let me recount:
    // venta 2: 01-10 → IN
    // venta 5: 01-08 → OUT (before 01-10)
    // venta 3: 01-12 → IN
    // venta 6: 01-14 → IN
    // venta 1: 01-15 → OUT
    // = 3 ventas
    // =========================================================================
    @Test
    void test_resumen_conFiltroFechas() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/reportes/resumen")
                .header("Authorization", "Bearer " + token)
                .param("desde", "2025-01-10T00:00:00Z")
                .param("hasta", "2025-01-14T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventasPorDia.length()").value(3))
                .andExpect(jsonPath("$.montoTotalAprobadas").value(3500.00));
    }

    // =========================================================================
    // Test 4: SUPERVISOR filtra solo en rango donde hay APROBADA
    // desde=2025-01-05T00:00:00Z, hasta=2025-01-09T23:59:59Z
    // venta 8: 01-05 APROBADA 9000.00 → IN
    // venta 5: 01-08 APROBADA 1500.00 → IN
    // = monto_total_aprobadas = 10500.00
    // =========================================================================
    @Test
    void test_resumen_montoAprobadasFiltrado() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/reportes/resumen")
                .header("Authorization", "Bearer " + token)
                .param("desde", "2025-01-05T00:00:00Z")
                .param("hasta", "2025-01-09T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoTotalAprobadas").value(10500.00));
    }

    // =========================================================================
    // Test 5: BACKOFFICE intenta acceder -> 403
    // =========================================================================
    @Test
    void test_resumen_backoffice_403() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");

        mockMvc.perform(get("/api/v1/reportes/resumen")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 6: AGENTE intenta acceder -> 403
    // =========================================================================
    @Test
    void test_resumen_agente_403() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        mockMvc.perform(get("/api/v1/reportes/resumen")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 7: Sin token -> 401
    // =========================================================================
    @Test
    void test_resumen_sinToken_401() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/resumen"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Test 8: SUPERVISOR con role claim manipulado -> 403 (authorities de BD)
    // =========================================================================
    @Test
    void test_resumen_roleClaimManipulado_403() throws Exception {
        String token = generateToken("agente1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/reportes/resumen")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 9: SUPERVISOR verifica serie ventas_por_dia con fecha y monto
    // =========================================================================
    @Test
    void test_resumen_ventasPorDia_conFechaYMonto() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/reportes/resumen")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventasPorDia[0].fecha").isString())
                .andExpect(jsonPath("$.ventasPorDia[0].count").isNumber())
                .andExpect(jsonPath("$.ventasPorDia[0].monto").isNumber());
    }

    // =========================================================================
    // Test 10: SUPERVISOR sin rango -> todas las ventas del equipo (9)
    // =========================================================================
    @Test
    void test_resumen_sinRango_completo() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/reportes/resumen")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventasPorDia.length()").value(9));
    }
}
