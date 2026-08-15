package com.telco.backend.domain.exception;

public class SaleNotPendingException extends RuntimeException {
    public SaleNotPendingException(Long id, String estado) {
        super("Venta " + id + " no está en estado PENDIENTE (actual: " + estado + ")");
    }
}
