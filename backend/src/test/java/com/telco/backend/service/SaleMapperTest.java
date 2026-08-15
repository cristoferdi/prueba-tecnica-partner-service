package com.telco.backend.service;

import com.telco.backend.domain.Role;
import com.telco.backend.domain.Sale;
import com.telco.backend.domain.SaleStatus;
import com.telco.backend.domain.User;
import com.telco.backend.web.dto.SaleResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SaleMapperTest {

    private final SaleMapper saleMapper = new SaleMapper();

    @Test
    void mapsAllFieldsFromSaleToResponse() {
        User agente = new User();
        agente.setId(4L);
        agente.setUsername("agente1");

        Sale sale = new Sale();
        sale.setId(1L);
        sale.setAgente(agente);
        sale.setDniCliente("12345678");
        sale.setNombreCliente("Juan Pérez Ríos");
        sale.setTelefonoCliente("987654321");
        sale.setDireccionCliente("Av. Larco 123, Lima");
        sale.setPlanActual("Plan Básico");
        sale.setPlanNuevo("Plan Premium");
        sale.setCodigoLlamada("CALL-20250115-0001");
        sale.setProducto("Internet");
        sale.setMonto(BigDecimal.valueOf(5000.00));
        sale.setEstado(SaleStatus.PENDIENTE);
        sale.setMotivoRechazo(null);
        Instant fechaRegistro = Instant.parse("2025-01-15T10:30:00Z");
        sale.setFechaRegistro(fechaRegistro);
        sale.setCreatedAt(fechaRegistro);
        sale.setUpdatedAt(fechaRegistro);

        SaleResponse response = saleMapper.toResponse(sale);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAgenteId()).isEqualTo(4L);
        assertThat(response.getAgenteUsername()).isEqualTo("agente1");
        assertThat(response.getDniCliente()).isEqualTo("12345678");
        assertThat(response.getNombreCliente()).isEqualTo("Juan Pérez Ríos");
        assertThat(response.getTelefonoCliente()).isEqualTo("987654321");
        assertThat(response.getDireccionCliente()).isEqualTo("Av. Larco 123, Lima");
        assertThat(response.getPlanActual()).isEqualTo("Plan Básico");
        assertThat(response.getPlanNuevo()).isEqualTo("Plan Premium");
        assertThat(response.getCodigoLlamada()).isEqualTo("CALL-20250115-0001");
        assertThat(response.getProducto()).isEqualTo("Internet");
        assertThat(response.getMonto()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        assertThat(response.getEstado()).isEqualTo(SaleStatus.PENDIENTE);
        assertThat(response.getMotivoRechazo()).isNull();
        assertThat(response.getFechaRegistro()).isEqualTo(fechaRegistro);
        assertThat(response.getFechaValidacion()).isNull();
        assertThat(response.getCreatedAt()).isEqualTo(fechaRegistro);
        assertThat(response.getUpdatedAt()).isEqualTo(fechaRegistro);
    }

    @Test
    void mapsNullAgentAsNullFields() {
        Sale sale = new Sale();
        sale.setId(2L);
        sale.setDniCliente("23456789");
        sale.setNombreCliente("María Quispe Chau");
        sale.setTelefonoCliente("912345678");
        sale.setDireccionCliente("Jr. San Martín 456, Arequipa");
        sale.setPlanActual("Plan Premium");
        sale.setPlanNuevo("Plan Empresarial");
        sale.setCodigoLlamada("CALL-20250110-0002");
        sale.setProducto("Fibra Óptica");
        sale.setMonto(BigDecimal.valueOf(3500.00));
        sale.setEstado(SaleStatus.APROBADA);
        sale.setFechaRegistro(Instant.parse("2025-01-10T09:00:00Z"));

        SaleResponse response = saleMapper.toResponse(sale);

        assertThat(response.getAgenteId()).isNull();
        assertThat(response.getAgenteUsername()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();
    }
}
