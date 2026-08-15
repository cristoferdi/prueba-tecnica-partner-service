package com.telco.backend.venta.dto;

import com.telco.backend.model.SaleStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class SaleFilter {
    private SaleStatus estado;
    private Instant desde;
    private Instant hasta;
}
