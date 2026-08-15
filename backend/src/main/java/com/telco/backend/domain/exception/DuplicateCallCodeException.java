package com.telco.backend.domain.exception;

public class DuplicateCallCodeException extends RuntimeException {
    public DuplicateCallCodeException(String codigoLlamada) {
        super("Código de llamada ya existe: " + codigoLlamada);
    }
}