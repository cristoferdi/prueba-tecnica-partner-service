package com.telco.backend;

import com.telco.backend.domain.Role;
import com.telco.backend.domain.Sale;
import com.telco.backend.domain.SaleStatus;
import com.telco.backend.domain.User;
import com.telco.backend.repository.SaleRepository;
import com.telco.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class PersistenceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Test
    void shouldFindAllSeedUsers() {
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(6);
    }

    @Test
    void shouldFindSupervisorCorrectly() {
        User supervisor = userRepository.findById(1L).orElseThrow();
        assertThat(supervisor.getUsername()).isEqualTo("supervisor1");
        assertThat(supervisor.getRole()).isEqualTo(Role.SUPERVISOR);
        assertThat(supervisor.getSupervisor()).isNull();
    }

    @Test
    void shouldFindAgenteWithSupervisor() {
        User agente = userRepository.findById(4L).orElseThrow();
        assertThat(agente.getUsername()).isEqualTo("agente1");
        assertThat(agente.getRole()).isEqualTo(Role.AGENTE);
        assertThat(agente.getSupervisor()).isNotNull();
        assertThat(agente.getSupervisor().getUsername()).isEqualTo("supervisor1");
    }

    @Test
    void shouldFindAllSeedSales() {
        List<Sale> sales = saleRepository.findAll();
        assertThat(sales).hasSize(9);
    }

    @Test
    void shouldReadEnumValuesFromDatabase() {
        Sale pendingSale = saleRepository.findById(1L).orElseThrow();
        assertThat(pendingSale.getEstado()).isEqualTo(SaleStatus.PENDIENTE);

        Sale approvedSale = saleRepository.findById(2L).orElseThrow();
        assertThat(approvedSale.getEstado()).isEqualTo(SaleStatus.APROBADA);

        Sale rejectedSale = saleRepository.findById(3L).orElseThrow();
        assertThat(rejectedSale.getEstado()).isEqualTo(SaleStatus.RECHAZADA);
        assertThat(rejectedSale.getMotivoRechazo()).isEqualTo("DNI no verificable");
    }

    @Test
    void shouldReadSaleFieldsFromDatabase() {
        Sale sale = saleRepository.findById(1L).orElseThrow();
        assertThat(sale.getCodigoLlamada()).isEqualTo("CALL-20250115-0001");
        assertThat(sale.getMonto()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        assertThat(sale.getAgente()).isNotNull();
        assertThat(sale.getAgente().getUsername()).isEqualTo("agente1");
        assertThat(sale.getFechaValidacion()).isNull();
    }

    @Test
    void shouldFindSalesByDifferentAgents() {
        List<Sale> sales = saleRepository.findAll();
        long agente1Sales = sales.stream().filter(s -> s.getAgente().getId() == 4L).count();
        long agente2Sales = sales.stream().filter(s -> s.getAgente().getId() == 5L).count();
        assertThat(agente1Sales + agente2Sales).isEqualTo(9);
    }

    // --- Write tests: verify JPA INSERT via @Enumerated(EnumType.STRING) -> VARCHAR ---

    @Test
    void shouldSaveSaleViaJpa() {
        User agente = userRepository.findById(4L).orElseThrow();

        Sale newSale = new Sale();
        newSale.setAgente(agente);
        newSale.setDniCliente("11223344");
        newSale.setNombreCliente("Test Cliente QA");
        newSale.setTelefonoCliente("999887766");
        newSale.setDireccionCliente("Calle de Prueba 123, Lima");
        newSale.setPlanActual("Plan Básico");
        newSale.setPlanNuevo("Plan Empresarial");
        newSale.setCodigoLlamada("CALL-TEST-0001");
        newSale.setProducto("Fibra Óptica");
        newSale.setMonto(BigDecimal.valueOf(3300.00));
        newSale.setEstado(SaleStatus.PENDIENTE);
        newSale.setFechaRegistro(Instant.parse("2025-01-25T10:00:00Z"));

        saleRepository.save(newSale);
        saleRepository.flush();

        assertThat(newSale.getId()).isNotNull();
        assertThat(newSale.getCreatedAt()).isNotNull();

        Sale savedSale = saleRepository.findById(newSale.getId()).orElseThrow();
        assertThat(savedSale.getCodigoLlamada()).isEqualTo("CALL-TEST-0001");
        assertThat(savedSale.getEstado()).isEqualTo(SaleStatus.PENDIENTE);
        assertThat(savedSale.getMonto()).isEqualByComparingTo(BigDecimal.valueOf(3300.00));
    }

    @Test
    void shouldUpdateSaleStatusViaJpa() {
        Sale sale = saleRepository.findById(1L).orElseThrow();
        assertThat(sale.getEstado()).isEqualTo(SaleStatus.PENDIENTE);

        sale.setEstado(SaleStatus.APROBADA);
        sale.setFechaValidacion(Instant.parse("2025-01-26T14:00:00Z"));
        sale.setMotivoRechazo(null);
        saleRepository.saveAndFlush(sale);

        Sale updatedSale = saleRepository.findById(1L).orElseThrow();
        assertThat(updatedSale.getEstado()).isEqualTo(SaleStatus.APROBADA);
        assertThat(updatedSale.getFechaValidacion()).isNotNull();
    }

    @Test
    void shouldSaveUserViaJpa() {
        User supervisor = userRepository.findById(1L).orElseThrow();

        User newUser = new User();
        newUser.setUsername("test_agente");
        newUser.setPasswordHash("$2b$10$testHashPlaceholderForJpaWriteTest12345678");
        newUser.setRole(Role.AGENTE);
        newUser.setSupervisor(supervisor);
        newUser.setActivo(true);

        userRepository.save(newUser);
        userRepository.flush();

        assertThat(newUser.getId()).isNotNull();
        assertThat(newUser.getCreatedAt()).isNotNull();
        assertThat(newUser.getSupervisor().getUsername()).isEqualTo("supervisor1");

        User savedUser = userRepository.findById(newUser.getId()).orElseThrow();
        assertThat(savedUser.getUsername()).isEqualTo("test_agente");
        assertThat(savedUser.getRole()).isEqualTo(Role.AGENTE);
    }
}
