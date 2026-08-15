package com.telco.backend.web;

import com.telco.backend.service.SaleService;
import com.telco.backend.web.dto.RejectSaleRequest;
import com.telco.backend.web.dto.SaleFilter;
import com.telco.backend.web.dto.SalePageResponse;
import com.telco.backend.web.dto.SaleRequest;
import com.telco.backend.web.dto.SaleResponse;
import com.telco.backend.web.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Ventas", description = "Gestión de ventas: creación, consulta, aprobación y rechazo")
public class SaleController {

    private final SaleService saleService;
    private final SaleStatusParamParser saleStatusParamParser;

    @PostMapping
    @Operation(summary = "Crear venta", description = "Registra una venta nueva en estado PENDIENTE. Solo AGENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta creada",
                    content = @Content(schema = @Schema(implementation = SaleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validación fallida",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Solo agentes",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "codigo_llamada duplicado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse createSale(@Valid @RequestBody SaleRequest request) {
        return saleService.createSale(request);
    }

    @GetMapping("/mis-ventas")
    @Operation(summary = "Mis ventas", description = "Lista las ventas del agente autenticado. Filtros y paginación. Solo AGENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de ventas",
                    content = @Content(schema = @Schema(implementation = SalePageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estado inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Solo agentes",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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
    @Operation(summary = "Ventas pendientes", description = "Lista todas las ventas PENDIENTES. Solo BACKOFFICE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ventas pendientes",
                    content = @Content(schema = @Schema(implementation = SaleResponse.class))),
            @ApiResponse(responseCode = "403", description = "Solo backoffice",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<SaleResponse> getPendingSales() {
        return saleService.getPendingSales();
    }

    @PostMapping("/{id}/aprobar")
    @Operation(summary = "Aprobar venta", description = "Aprueba una venta PENDIENTE y fija fecha_validacion. Solo BACKOFFICE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta aprobada",
                    content = @Content(schema = @Schema(implementation = SaleResponse.class))),
            @ApiResponse(responseCode = "400", description = "La venta no está PENDIENTE",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Solo backoffice",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public SaleResponse approveSale(@PathVariable Long id) {
        return saleService.approveSale(id);
    }

    @PostMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar venta", description = "Rechaza una venta PENDIENTE con motivo y fija fecha_validacion. Solo BACKOFFICE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta rechazada",
                    content = @Content(schema = @Schema(implementation = SaleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Motivo requerido o la venta no está PENDIENTE",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Solo backoffice",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public SaleResponse rejectSale(@PathVariable Long id, @Valid @RequestBody RejectSaleRequest request) {
        return saleService.rejectSale(id, request.getMotivo());
    }

    @GetMapping("/equipo")
    @Operation(summary = "Ventas del equipo", description = "Ventas de los agentes bajo la supervisión del usuario autenticado. Solo SUPERVISOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ventas del equipo",
                    content = @Content(schema = @Schema(implementation = SaleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estado inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Solo supervisores",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<SaleResponse> getEquipoVentas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long agenteId,
            @RequestParam(required = false) Instant desde,
            @RequestParam(required = false) Instant hasta) {

        return saleService.getEquipoVentas(saleStatusParamParser.parse(estado), agenteId, desde, hasta);
    }
}
