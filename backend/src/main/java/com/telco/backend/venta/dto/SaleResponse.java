package com.telco.backend.venta.dto;

import com.telco.backend.model.SaleStatus;
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
    private String createdAt;
    private String updatedAt;
}