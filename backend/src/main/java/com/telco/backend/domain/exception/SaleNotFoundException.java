package com.telco.backend.domain.exception;

public class SaleNotFoundException extends RuntimeException {
    public SaleNotFoundException(Long id) {
        super("Venta no encontrada con id: " + id);
    }
}
