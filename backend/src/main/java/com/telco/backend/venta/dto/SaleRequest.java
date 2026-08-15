package com.telco.backend.venta.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SaleRequest {

    @NotBlank(message = "DNI cliente es requerido")
    @Pattern(regexp = "\\d{8}|\\d{11}", message = "DNI debe tener 8 o 11 dígitos")
    private String dniCliente;

    @NotBlank(message = "Nombre cliente es requerido")
    @Size(max = 100, message = "Nombre cliente máximo 100 caracteres")
    private String nombreCliente;

    @NotBlank(message = "Teléfono cliente es requerido")
    @Pattern(regexp = "\\d{9}", message = "Teléfono debe tener 9 dígitos")
    private String telefonoCliente;

    @NotBlank(message = "Dirección cliente es requerida")
    @Size(max = 200, message = "Dirección cliente máximo 200 caracteres")
    private String direccionCliente;

    @NotBlank(message = "Plan actual es requerido")
    @Size(max = 50, message = "Plan actual máximo 50 caracteres")
    private String planActual;

    @NotBlank(message = "Plan nuevo es requerido")
    @Size(max = 50, message = "Plan nuevo máximo 50 caracteres")
    private String planNuevo;

    @NotBlank(message = "Código de llamada es requerido")
    @Size(max = 50, message = "Código de llamada máximo 50 caracteres")
    private String codigoLlamada;

    @NotBlank(message = "Producto es requerido")
    @Size(max = 100, message = "Producto máximo 100 caracteres")
    private String producto;

    @NotNull(message = "Monto es requerido")
    @DecimalMin(value = "0.01", message = "Monto debe ser mayor a 0")
    private BigDecimal monto;
}