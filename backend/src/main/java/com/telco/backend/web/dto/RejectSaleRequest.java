package com.telco.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectSaleRequest {

    @NotBlank(message = "Motivo es requerido")
    private String motivo;
}
