package com.telco.backend.service;

import com.telco.backend.domain.SaleStatus;
import com.telco.backend.domain.User;
import com.telco.backend.repository.SaleRepository;
import com.telco.backend.web.dto.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleRepository saleRepository;
    private final CurrentUserService currentUserService;

    public ReportResponse getResumen(Instant desde, Instant hasta) {
        User supervisor = currentUserService.getCurrentAuthenticatedUser();

        List<com.telco.backend.web.dto.StatusCount> conteosPorEstado =
                saleRepository.countByEstadoForSupervisor(supervisor.getId(), desde, hasta);
        BigDecimal montoTotalAprobadas =
                saleRepository.sumMontoByEstadoForSupervisor(supervisor.getId(), SaleStatus.APROBADA, desde, hasta);
        List<com.telco.backend.web.dto.SalesPerDay> ventasPorDia =
                saleRepository.seriesPorDiaForSupervisor(supervisor.getId(), desde, hasta);

        ReportResponse response = new ReportResponse();
        response.setConteosPorEstado(conteosPorEstado);
        response.setMontoTotalAprobadas(montoTotalAprobadas);
        response.setVentasPorDia(ventasPorDia);
        return response;
    }
}
