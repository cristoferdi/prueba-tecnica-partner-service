package com.telco.backend.web;

import com.telco.backend.service.ReportService;
import com.telco.backend.web.dto.ReportResponse;
import com.telco.backend.web.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Resumen de ventas del equipo para supervisores")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/resumen")
    @Operation(summary = "Resumen de ventas", description = """
            Resumen de las ventas del equipo del supervisor autenticado:
            conteos por estado, monto total aprobadas y serie de ventas por día.
            Opcionalmente se filtra por rango [desde, hasta]. Solo SUPERVISOR.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen",
                    content = @Content(schema = @Schema(implementation = ReportResponse.class))),
            @ApiResponse(responseCode = "403", description = "Solo supervisores",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ReportResponse getResumen(
            @RequestParam(required = false) Instant desde,
            @RequestParam(required = false) Instant hasta) {
        return reportService.getResumen(desde, hasta);
    }
}
