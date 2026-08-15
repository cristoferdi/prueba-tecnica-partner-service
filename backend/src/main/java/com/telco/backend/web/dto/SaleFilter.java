package com.telco.backend.web.dto;

import com.telco.backend.domain.SaleStatus;
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
