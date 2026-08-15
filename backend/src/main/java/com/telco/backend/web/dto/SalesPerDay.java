package com.telco.backend.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class SalesPerDay {
    private String fecha;
    private Long count;
    private BigDecimal monto;

    public SalesPerDay(java.sql.Date fecha, Long count, BigDecimal monto) {
        this.fecha = fecha.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        this.count = count;
        this.monto = monto;
    }
}
