package com.telco.backend.service;

import com.telco.backend.domain.SaleStatus;
import com.telco.backend.domain.User;
import com.telco.backend.repository.SaleRepository;
import com.telco.backend.web.dto.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final Instant FECHA_MIN = LocalDate.of(1900, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    private static final Instant FECHA_MAX = LocalDate.of(2100, 12, 31).atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

    private final SaleRepository saleRepository;
    private final CurrentUserService currentUserService;

    public ReportResponse getResumen(Instant desde, Instant hasta) {
        User supervisor = currentUserService.getCurrentAuthenticatedUser();

        Instant desdeNorm = desde != null ? desde : FECHA_MIN;
        Instant hastaNorm = hasta != null ? hasta : FECHA_MAX;

        List<com.telco.backend.web.dto.StatusCount> conteosPorEstado =
                saleRepository.countByEstadoForSupervisor(supervisor.getId(), desdeNorm, hastaNorm);
        BigDecimal montoTotalAprobadas =
                saleRepository.sumMontoByEstadoForSupervisor(supervisor.getId(), SaleStatus.APROBADA, desdeNorm, hastaNorm);
        List<com.telco.backend.web.dto.SalesPerDay> ventasPorDia =
                saleRepository.seriesPorDiaForSupervisor(supervisor.getId(), desdeNorm, hastaNorm);

        ReportResponse response = new ReportResponse();
        response.setConteosPorEstado(conteosPorEstado);
        response.setMontoTotalAprobadas(montoTotalAprobadas);
        response.setVentasPorDia(ventasPorDia);
        return response;
    }
}
