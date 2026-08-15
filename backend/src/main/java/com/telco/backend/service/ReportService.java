package com.telco.backend.service;

import com.telco.backend.domain.SaleStatus;
import com.telco.backend.domain.User;
import com.telco.backend.repository.SaleRepository;
import com.telco.backend.repository.UserRepository;
import com.telco.backend.security.user.CustomUserDetails;
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
    private final UserRepository userRepository;

    public ReportResponse getResumen(Instant desde, Instant hasta) {
        User supervisor = getCurrentAuthenticatedUser();

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

    private User getCurrentAuthenticatedUser() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en BD"));
    }
}
