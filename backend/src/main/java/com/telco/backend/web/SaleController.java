package com.telco.backend.web;

import com.telco.backend.domain.SaleStatus;
import com.telco.backend.service.SaleService;
import com.telco.backend.web.dto.RejectSaleRequest;
import com.telco.backend.web.dto.SaleFilter;
import com.telco.backend.web.dto.SalePageResponse;
import com.telco.backend.web.dto.SaleRequest;
import com.telco.backend.web.dto.SaleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse createSale(@Valid @RequestBody SaleRequest request) {
        return saleService.createSale(request);
    }

    @GetMapping("/mis-ventas")
    public SalePageResponse getMisVentas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Instant desde,
            @RequestParam(required = false) Instant hasta,
            Pageable pageable) {

        SaleFilter filter = new SaleFilter();
        if (estado != null && !estado.isEmpty()) {
            try {
                filter.setEstado(SaleStatus.valueOf(estado));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido: " + estado);
            }
        }
        filter.setDesde(desde);
        filter.setHasta(hasta);

        return saleService.getMisVentas(filter, pageable);
    }

    @GetMapping("/pendientes")
    public List<SaleResponse> getPendingSales() {
        return saleService.getPendingSales();
    }

    @PostMapping("/{id}/aprobar")
    public SaleResponse approveSale(@PathVariable Long id) {
        return saleService.approveSale(id);
    }

    @PostMapping("/{id}/rechazar")
    public SaleResponse rejectSale(@PathVariable Long id, @Valid @RequestBody RejectSaleRequest request) {
        return saleService.rejectSale(id, request.getMotivo());
    }

    @GetMapping("/equipo")
    public List<SaleResponse> getEquipoVentas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long agenteId,
            @RequestParam(required = false) Instant desde,
            @RequestParam(required = false) Instant hasta) {

        SaleStatus estadoEnum = null;
        if (estado != null && !estado.isEmpty()) {
            try {
                estadoEnum = SaleStatus.valueOf(estado);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido: " + estado);
            }
        }

        return saleService.getEquipoVentas(estadoEnum, agenteId, desde, hasta);
    }
}
