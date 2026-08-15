package com.telco.backend.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class SalesPerDay {
    private String fecha;
    private Long count;
    private BigDecimal monto;
}
