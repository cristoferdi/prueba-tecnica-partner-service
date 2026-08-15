package com.telco.backend.reporte.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class VentaPorDia {
    private String fecha;
    private Long count;
    private BigDecimal monto;
}
