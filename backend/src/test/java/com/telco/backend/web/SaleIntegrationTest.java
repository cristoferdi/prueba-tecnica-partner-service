package com.telco.backend.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telco.backend.support.AbstractPostgresContainerTest;
import com.telco.backend.web.dto.RejectSaleRequest;
import com.telco.backend.web.dto.SaleRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class SaleIntegrationTest extends AbstractPostgresContainerTest {

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

    private ResultActions createSale(SaleRequest request, String token) throws Exception {
        return mockMvc.perform(post("/api/v1/ventas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions getMisVentas(String token, String... params) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder =
                get("/api/v1/ventas/mis-ventas")
                        .header("Authorization", "Bearer " + token);
        for (int i = 0; i < params.length; i += 2) {
            builder.param(params[i], params[i + 1]);
        }
        return mockMvc.perform(builder);
    }

    private SaleRequest validSaleRequest(String codigoLlamada) {
        SaleRequest request = new SaleRequest();
        request.setDniCliente("12345678");
        request.setNombreCliente("Juan Pérez Ríos");
        request.setTelefonoCliente("987654321");
        request.setDireccionCliente("Av. Larco 123, Lima");
        request.setPlanActual("Plan Básico");
        request.setPlanNuevo("Plan Premium");
        request.setCodigoLlamada(codigoLlamada);
        request.setProducto("Internet");
        request.setMonto(BigDecimal.valueOf(5000.00));
        return request;
    }

    // =========================================================================
    // Test 1: AGENTE crea venta exitosamente -> 201, estado PENDIENTE
    // =========================================================================
    @Test
    void test_crearVenta_exitoso() throws Exception {
        String token = generateToken("agente1", "AGENTE");
        SaleRequest request = validSaleRequest("CALL-NEW-0001");

        ResultActions result = createSale(request, token);

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.codigoLlamada").value("CALL-NEW-0001"))
                .andExpect(jsonPath("$.dniCliente").value("12345678"))
                .andExpect(jsonPath("$.telefonoCliente").value("987654321"))
                .andExpect(jsonPath("$.agenteUsername").value("agente1"))
                .andExpect(jsonPath("$.monto").value(5000.00));
    }

    // =========================================================================
    // Test 2: usuario no autenticado -> 401
    // =========================================================================
    @Test
    void test_crearVenta_sinToken_401() throws Exception {
        SaleRequest request = validSaleRequest("CALL-NEW-0002");

        mockMvc.perform(post("/api/v1/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Test 3: usuario no AGENTE -> 403
    // =========================================================================
    @Test
    void test_crearVenta_noAgente_403() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");
        SaleRequest request = validSaleRequest("CALL-NEW-0003");

        createSale(request, token)
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 4: DNI inválido (no 8 dígitos) -> 400
    // =========================================================================
    @Test
    void test_crearVenta_dniInvalido_400() throws Exception {
        String token = generateToken("agente1", "AGENTE");
        SaleRequest request = validSaleRequest("CALL-NEW-0004");
        request.setDniCliente("123");

        createSale(request, token)
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Test 5: Teléfono inválido (no 9 dígitos) -> 400
    // =========================================================================
    @Test
    void test_crearVenta_telefonoInvalido_400() throws Exception {
        String token = generateToken("agente1", "AGENTE");
        SaleRequest request = validSaleRequest("CALL-NEW-0005");
        request.setTelefonoCliente("12345");

        createSale(request, token)
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Test 6: Campo de texto vacío -> 400
    // =========================================================================
    @Test
    void test_crearVenta_nombreVacio_400() throws Exception {
        String token = generateToken("agente1", "AGENTE");
        SaleRequest request = validSaleRequest("CALL-NEW-0006");
        request.setNombreCliente("");

        createSale(request, token)
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Test 7: codigo_llamada duplicado -> 409
    // =========================================================================
    @Test
    void test_crearVenta_codigoDuplicado_409() throws Exception {
        String token = generateToken("agente1", "AGENTE");
        SaleRequest request = validSaleRequest("CALL-20250115-0001");

        createSale(request, token)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    // =========================================================================
    // Test 8: DNI con 11 dígitos (RUC) → 201 (válido)
    // =========================================================================
    @Test
    void test_crearVenta_dni11Digitos_exitoso() throws Exception {
        String token = generateToken("agente1", "AGENTE");
        SaleRequest request = validSaleRequest("CALL-NEW-0007");
        request.setDniCliente("12345678901");

        createSale(request, token)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dniCliente").value("12345678901"));
    }

    // =========================================================================
    // Test 9: campos required faltan -> 400
    // =========================================================================
    @Test
    void test_crearVenta_camposFaltan_400() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        mockMvc.perform(post("/api/v1/ventas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Test 10: monto inválido -> 400
    // =========================================================================
    @Test
    void test_crearVenta_montoInvalido_400() throws Exception {
        String token = generateToken("agente1", "AGENTE");
        SaleRequest request = validSaleRequest("CALL-NEW-0008");
        request.setMonto(BigDecimal.ZERO);

        createSale(request, token)
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Test 11: AGENTE puede crear múltiples ventas
    // =========================================================================
    @Test
    void test_crearVenta_multiples_exitoso() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        SaleRequest req1 = validSaleRequest("CALL-NEWMULTI-001");
        createSale(req1, token).andExpect(status().isCreated());

        SaleRequest req2 = validSaleRequest("CALL-NEWMULTI-002");
        createSale(req2, token).andExpect(status().isCreated());
    }

    // =========================================================================
    // Test 12: usuario manipulado (role claim AGENTE pero usuario real es BACKOFFICE)
    // No debe poder crear ventas -> 403 (authorities vienen de BD, no del JWT)
    // =========================================================================
    @Test
    void test_crearVenta_roleClaimManipulado_403() throws Exception {
        String token = generateToken("back1", "AGENTE");
        SaleRequest request = validSaleRequest("CALL-NEW-0009");

        createSale(request, token)
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 13: AGENTE lista sus ventas sin filtros -> 200
    // agente1 (id=4) owns seed ventas 1, 3, 5, 6, 9 = 5 total
    // =========================================================================
    @Test
    void test_misVentas_sinFiltros_exitoso() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        getMisVentas(token, "page", "0", "size", "10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.content[0].agenteUsername").value("agente1"));
    }

    // =========================================================================
    // Test 14: AGENTE filtra por estado PENDIENTE -> solo ventas PENDIENTE
    // agente1 PENDIENTE: ventas 1, 9 = 2
    // =========================================================================
    @Test
    void test_misVentas_filtroEstado_exitoso() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        getMisVentas(token, "estado", "PENDIENTE", "page", "0", "size", "10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].estado").value("PENDIENTE"));
    }

    // =========================================================================
    // Test 15: AGENTE filtra por estado APROBADA -> solo ventas APROBADA
    // agente1 APROBADA: venta 5 = 1
    // =========================================================================
    @Test
    void test_misVentas_filtroEstadoAprobadas_exitoso() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        getMisVentas(token, "estado", "APROBADA", "page", "0", "size", "10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].estado").value("APROBADA"));
    }

    // =========================================================================
    // Test 16: AGENTE filtra por rango de fechas -> 200, contenido filtrado
    // desde=2025-01-10T00:00:00Z, hasta=2025-01-15T23:59:59Z
    // agente1 ventas in range: venta 1 (01-15), venta 3 (01-12), venta 6 (01-14) = 3
    // =========================================================================
    @Test
    void test_misVentas_filtroFechas_exitoso() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        getMisVentas(token, "desde", "2025-01-10T00:00:00Z", "hasta", "2025-01-15T23:59:59Z", "page", "0", "size", "10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[*].fechaRegistro").exists());
    }

    // =========================================================================
    // Test 17: AGENTE combina filtros estado + fecha -> 200
    // estado=PENDIENTE, desde=2025-01-01, hasta=2025-12-31
    // agente1 PENDIENTE en range: ventas 1 (01-15), 9 (01-22) = 2
    // =========================================================================
    @Test
    void test_misVentas_filtroEstadoYFechas_exitoso() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        getMisVentas(token, "estado", "PENDIENTE", "desde", "2025-01-01T00:00:00Z", "hasta", "2025-12-31T23:59:59Z", "page", "0", "size", "10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    // =========================================================================
    // Test 18: Paginación con size=2 -> 2 elementos en content, 3 páginas
    // agente1 has 5 total -> ceil(5/2) = 3 pages
    // =========================================================================
    @Test
    void test_misVentas_paginacion_exitoso() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        getMisVentas(token, "page", "0", "size", "2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    // =========================================================================
    // Test 19: Ordenamiento por fechaRegistro,desc -> primera venta es la más nueva
    // =========================================================================
    @Test
    void test_misVentas_sortDesc_exitoso() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        getMisVentas(token, "sort", "fechaRegistro,desc", "page", "0", "size", "10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fechaRegistro").isNotEmpty());
    }

    // =========================================================================
    // Test 20: AGENTE solo ve sus ventas, NO ve las de otros agentes
    // agente2 (id=5) owns seed ventas 2, 4, 7, 8 = 4 total
    // =========================================================================
    @Test
    void test_misVentas_soloPropias_exitoso() throws Exception {
        String token = generateToken("agente2", "AGENTE");

        getMisVentas(token, "page", "0", "size", "10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content[0].agenteUsername").value("agente2"));
    }

    // =========================================================================
    // Test 21: usuario no autenticado -> 401
    // =========================================================================
    @Test
    void test_misVentas_sinToken_401() throws Exception {
        mockMvc.perform(get("/api/v1/ventas/mis-ventas"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Test 22: BACKOFFICE intenta acceder -> 403
    // =========================================================================
    @Test
    void test_misVentas_noAgente_403() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");

        mockMvc.perform(get("/api/v1/ventas/mis-ventas")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 23: AGENTE con role claim manipulado -> 403 (authorities de BD)
    // =========================================================================
    @Test
    void test_misVentas_roleClaimManipulado_403() throws Exception {
        String token = generateToken("back1", "AGENTE");

        mockMvc.perform(get("/api/v1/ventas/mis-ventas")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 24: estado inválido -> 400
    // =========================================================================
    @Test
    void test_misVentas_estadoInvalido_400() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        getMisVentas(token, "estado", "INVALIDO")
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Test 25: BACKOFFICE lista ventas PENDIENTES -> 200, 4 elementos
    // Seed data: ventas PENDIENTE = ids 1, 4, 7, 9
    // =========================================================================
    @Test
    void test_pendientes_lista_exitoso() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");

        mockMvc.perform(get("/api/v1/ventas/pendientes")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    // =========================================================================
    // Test 26: AGENTE intenta listar pendientes -> 403
    // =========================================================================
    @Test
    void test_pendientes_noBackoffice_403() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        mockMvc.perform(get("/api/v1/ventas/pendientes")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 27: BACKOFFICE aprueba venta PENDIENTE -> 200, estado APROBADA
    // =========================================================================
    @Test
    void test_aprobarVenta_exitoso() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");

        mockMvc.perform(post("/api/v1/ventas/1/aprobar")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"))
                .andExpect(jsonPath("$.fechaValidacion").isNotEmpty());
    }

    // =========================================================================
    // Test 28: BACKOFFICE rechaza venta PENDIENTE con motivo -> 200, estado RECHAZADA
    // =========================================================================
    @Test
    void test_rechazarVenta_exitoso() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");

        RejectSaleRequest request = new RejectSaleRequest();
        request.setMotivo("Datos incompletos del cliente");

        mockMvc.perform(post("/api/v1/ventas/1/rechazar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"))
                .andExpect(jsonPath("$.motivoRechazo").value("Datos incompletos del cliente"))
                .andExpect(jsonPath("$.fechaValidacion").isNotEmpty());
    }

    // =========================================================================
    // Test 29: BACKOFFICE rechaza sin motivo -> 400
    // =========================================================================
    @Test
    void test_rechazarVenta_sinMotivo_400() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");

        mockMvc.perform(post("/api/v1/ventas/1/rechazar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Test 30: BACKOFFICE aprueba venta inexistente -> 404
    // =========================================================================
    @Test
    void test_aprobarVenta_inexistente_404() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");

        mockMvc.perform(post("/api/v1/ventas/999/aprobar")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // =========================================================================
    // Test 31: BACKOFFICE rechaza venta inexistente -> 404
    // =========================================================================
    @Test
    void test_rechazarVenta_inexistente_404() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");

        RejectSaleRequest request = new RejectSaleRequest();
        request.setMotivo("Venta no existe");

        mockMvc.perform(post("/api/v1/ventas/999/rechazar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // Test 32: BACKOFFICE intenta aprobar venta ya APROBADA -> 400
    // venta 5 is APROBADA in seed data
    // =========================================================================
    @Test
    void test_aprobarVenta_yaAprobada_400() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");

        mockMvc.perform(post("/api/v1/ventas/5/aprobar")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // =========================================================================
    // Test 33: BACKOFFICE intenta rechazar venta ya RECHAZADA -> 400
    // venta 3 is RECHAZADA in seed data
    // =========================================================================
    @Test
    void test_rechazarVenta_yaRechazada_400() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");

        RejectSaleRequest request = new RejectSaleRequest();
        request.setMotivo("Intento de rechazar nuevamente");

        mockMvc.perform(post("/api/v1/ventas/3/rechazar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Test 34: BACKOFFICE sin token -> 401
    // =========================================================================
    @Test
    void test_aprobarVenta_sinToken_401() throws Exception {
        mockMvc.perform(post("/api/v1/ventas/1/aprobar"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Test 35: BACKOFFICE role claim manipulado -> 403 (authorities de BD)
    // =========================================================================
    @Test
    void test_aprobarVenta_roleClaimManipulado_403() throws Exception {
        String token = generateToken("agente1", "BACKOFFICE");

        mockMvc.perform(post("/api/v1/ventas/1/aprobar")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 36: AGENTE intenta aprobar -> 403
    // =========================================================================
    @Test
    void test_aprobarVenta_agente_403() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        mockMvc.perform(post("/api/v1/ventas/1/aprobar")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 37: BACKOFFICE lista pendientes, aprueba una, lista de nuevo -> 3 items
    // =========================================================================
    @Test
    void test_aprobarVenta_reducePendientes() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");

        mockMvc.perform(get("/api/v1/ventas/pendientes")
                .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.length()").value(4));

        mockMvc.perform(post("/api/v1/ventas/1/aprobar")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/ventas/pendientes")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    // =========================================================================
    // Test 38: SUPERVISOR lista ventas del equipo -> 200, 9 ventas totales
    // supervisor1 (id=1) supervisa agente1 (id=4) y agente2 (id=5)
    // =========================================================================
    @Test
    void test_equipo_ventas_exitoso() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(9));
    }

    // =========================================================================
    // Test 39: SUPERVISOR filtra por estado PENDIENTE -> 4 ventas
    // =========================================================================
    @Test
    void test_equipo_filtroEstado_exitoso() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token)
                .param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    // =========================================================================
    // Test 40: SUPERVISOR filtra por estado APROBADA -> 3 ventas
    // =========================================================================
    @Test
    void test_equipo_filtroEstadoAprobadas_exitoso() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token)
                .param("estado", "APROBADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    // =========================================================================
    // Test 41: SUPERVISOR filtra por estado RECHAZADA -> 2 ventas
    // =========================================================================
    @Test
    void test_equipo_filtroEstadoRechazadas_exitoso() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token)
                .param("estado", "RECHAZADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // =========================================================================
    // Test 42: SUPERVISOR filtra por agenteId -> 5 ventas (agente1)
    // =========================================================================
    @Test
    void test_equipo_filtroAgenteId_exitoso() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token)
                .param("agenteId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].agenteUsername").value("agente1"));
    }

    // =========================================================================
    // Test 43: SUPERVISOR filtra por rango de fechas
    // desde=2025-01-12, hasta=2025-01-18 -> ventas 1, 3, 4, 6 = 4 ventas
    // =========================================================================
    @Test
    void test_equipo_filtroFechas_exitoso() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token)
                .param("desde", "2025-01-12T00:00:00Z")
                .param("hasta", "2025-01-18T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    // =========================================================================
    // Test 44: SUPERVISOR combina filtros estado + agenteId + fechas
    // estado=PENDIENTE, agenteId=4, desde=2025-01-01, hasta=2025-12-31
    // agente1 PENDIENTE: ventas 1 (01-15), 9 (01-22) = 2
    // =========================================================================
    @Test
    void test_equipo_combinaFiltros_exitoso() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token)
                .param("estado", "PENDIENTE")
                .param("agenteId", "4")
                .param("desde", "2025-01-01T00:00:00Z")
                .param("hasta", "2025-12-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // =========================================================================
    // Test 45: SUPERVISOR con role claim manipulado -> 403 (authorities de BD)
    // =========================================================================
    @Test
    void test_equipo_roleClaimManipulado_403() throws Exception {
        String token = generateToken("agente1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 46: BACKOFFICE intenta acceder a equipo -> 403
    // =========================================================================
    @Test
    void test_equipo_backoffice_403() throws Exception {
        String token = generateToken("back1", "BACKOFFICE");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 47: AGENTE intenta acceder a equipo -> 403
    // =========================================================================
    @Test
    void test_equipo_agente_403() throws Exception {
        String token = generateToken("agente1", "AGENTE");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Test 48: SUPERVISOR sin token -> 401
    // =========================================================================
    @Test
    void test_equipo_sinToken_401() throws Exception {
        mockMvc.perform(get("/api/v1/ventas/equipo"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Test 49: SUPERVISOR con estado inválido -> 400
    // =========================================================================
    @Test
    void test_equipo_estadoInvalido_400() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token)
                .param("estado", "INVALIDO"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Test 50: SUPERVISOR con agenteId inexistente -> 200, lista vacía
    // =========================================================================
    @Test
    void test_equipo_agenteInexistente_listaVacia() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token)
                .param("agenteId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================================================================
    // Test 51 (REGRESIÓN): SUPERVISOR NO ve ventas de agentes de OTRO supervisor
    // supervisor1 (id=1) solo supervisa agente1 (id=4) y agente2 (id=5).
    // agente3 (id=8) pertenece a supervisor2 (id=7) -> lista vacía
    // =========================================================================
    @Test
    void test_equipo_noVeAgenteDeOtroSupervisor() throws Exception {
        String token = generateToken("supervisor1", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token)
                .param("agenteId", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================================================================
    // Test 52 (REGRESIÓN): supervisor2 ve solo las ventas de SU equipo
    // supervisor2 (id=7) supervisa agente3 (id=8) con la venta 10 -> 1 venta
    // =========================================================================
    @Test
    void test_equipo_supervisor2_soloSuEquipo() throws Exception {
        String token = generateToken("supervisor2", "SUPERVISOR");

        mockMvc.perform(get("/api/v1/ventas/equipo")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].agenteUsername").value("agente3"));
    }
}
