package com.telco.backend.web;

import com.telco.backend.domain.SaleStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SaleStatusParamParser {

    /**
     * Convierte el parámetro {@code estado} a {@link SaleStatus}.
     *
     * @return el enum, o {@code null} si el valor viene vacío/ausente.
     * @throws ResponseStatusException 400 si el valor no es un estado válido.
     */
    public SaleStatus parse(String estado) {
        if (estado == null || estado.isEmpty()) {
            return null;
        }
        try {
            return SaleStatus.valueOf(estado);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido: " + estado);
        }
    }
}
