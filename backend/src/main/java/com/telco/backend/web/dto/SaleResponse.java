package com.telco.backend.web.dto;

import com.telco.backend.domain.SaleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class SaleResponse {

    private Long id;
    private String dniCliente;
    private String nombreCliente;
    private String telefonoCliente;
    private String direccionCliente;
    private String planActual;
    private String planNuevo;
    private String codigoLlamada;
    private String producto;
    private BigDecimal monto;
    private SaleStatus estado;
    private String motivoRechazo;
    private Instant fechaRegistro;
    private Instant fechaValidacion;
    private Long agenteId;
    private String agenteUsername;
    private Instant createdAt;
    private Instant updatedAt;
}