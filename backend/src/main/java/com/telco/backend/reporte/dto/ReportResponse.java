package com.telco.backend.reporte.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReportResponse {
    private List<EstadoCount> conteosPorEstado;
    private BigDecimal montoTotalAprobadas;
    private List<VentaPorDia> ventasPorDia;
}
