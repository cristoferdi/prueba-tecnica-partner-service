package com.telco.backend.web;

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
    private final SaleStatusParamParser saleStatusParamParser;

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
        filter.setEstado(saleStatusParamParser.parse(estado));
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

        return saleService.getEquipoVentas(saleStatusParamParser.parse(estado), agenteId, desde, hasta);
    }
}
