package com.telco.backend.venta.exception;

public class DuplicateCodigoLlamadaException extends RuntimeException {
    public DuplicateCodigoLlamadaException(String codigoLlamada) {
        super("Código de llamada ya existe: " + codigoLlamada);
    }
}