package com.telco.backend.venta.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RechazarVentaRequest {

    @NotBlank(message = "Motivo es requerido")
    private String motivo;
}
