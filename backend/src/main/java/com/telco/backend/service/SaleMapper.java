package com.telco.backend.service;

import com.telco.backend.domain.Sale;
import com.telco.backend.web.dto.SaleResponse;
import org.springframework.stereotype.Component;

@Component
public class SaleMapper {

    public SaleResponse toResponse(Sale sale) {
        SaleResponse response = new SaleResponse();
        response.setId(sale.getId());
        response.setDniCliente(sale.getDniCliente());
        response.setNombreCliente(sale.getNombreCliente());
        response.setTelefonoCliente(sale.getTelefonoCliente());
        response.setDireccionCliente(sale.getDireccionCliente());
        response.setPlanActual(sale.getPlanActual());
        response.setPlanNuevo(sale.getPlanNuevo());
        response.setCodigoLlamada(sale.getCodigoLlamada());
        response.setProducto(sale.getProducto());
        response.setMonto(sale.getMonto());
        response.setEstado(sale.getEstado());
        response.setMotivoRechazo(sale.getMotivoRechazo());
        response.setFechaRegistro(sale.getFechaRegistro());
        response.setFechaValidacion(sale.getFechaValidacion());
        response.setCreatedAt(sale.getCreatedAt());
        response.setUpdatedAt(sale.getUpdatedAt());
        if (sale.getAgente() != null) {
            response.setAgenteId(sale.getAgente().getId());
            response.setAgenteUsername(sale.getAgente().getUsername());
        }
        return response;
    }
}
