package com.telco.backend.reporte.dto;

import com.telco.backend.model.SaleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EstadoCount {
    private SaleStatus estado;
    private Long count;
}
