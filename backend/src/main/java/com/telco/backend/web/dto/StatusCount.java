package com.telco.backend.web.dto;

import com.telco.backend.domain.SaleStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StatusCount {
    private SaleStatus estado;
    private Long count;
}
