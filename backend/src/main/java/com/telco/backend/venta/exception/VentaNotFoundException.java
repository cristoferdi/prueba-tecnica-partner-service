package com.telco.backend.venta.exception;

public class VentaNotFoundException extends RuntimeException {
    public VentaNotFoundException(Long id) {
        super("Venta no encontrada con id: " + id);
    }
}
