package com.telco.backend.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReportResponse {
    private List<StatusCount> conteosPorEstado;
    private BigDecimal montoTotalAprobadas;
    private List<SalesPerDay> ventasPorDia;
}
