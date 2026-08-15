package com.telco.backend.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Motivo de rechazo de venta (BACKOFFICE)", example = """
        {"motivo": "Datos incompletos del cliente"}""")
public class RejectSaleRequest {

    @NotBlank(message = "Motivo es requerido")
    private String motivo;
}
