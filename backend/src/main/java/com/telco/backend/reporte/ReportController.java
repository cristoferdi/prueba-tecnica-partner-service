package com.telco.backend.reporte;

import com.telco.backend.reporte.dto.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/resumen")
    public ReportResponse getResumen(
            @RequestParam(required = false) Instant desde,
            @RequestParam(required = false) Instant hasta) {
        return reportService.getResumen(desde, hasta);
    }
}
