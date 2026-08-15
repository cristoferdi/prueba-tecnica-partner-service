package com.telco.backend.venta.exception;

public class VentaNotPendingException extends RuntimeException {
    public VentaNotPendingException(Long id, String estado) {
        super("Venta " + id + " no está en estado PENDIENTE (actual: " + estado + ")");
    }
}
