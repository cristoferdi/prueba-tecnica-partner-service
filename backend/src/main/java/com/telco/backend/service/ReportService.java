package com.telco.backend.service;

import com.telco.backend.domain.Sale;
import com.telco.backend.domain.SaleStatus;
import com.telco.backend.domain.User;
import com.telco.backend.repository.SaleRepository;
import com.telco.backend.repository.UserRepository;
import com.telco.backend.repository.specification.SaleSpecs;
import com.telco.backend.security.user.CustomUserDetails;
import com.telco.backend.web.dto.ReportResponse;
import com.telco.backend.web.dto.SalesPerDay;
import com.telco.backend.web.dto.StatusCount;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final SaleSpecs saleSpecs;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    public ReportResponse getResumen(Instant desde, Instant hasta) {
        User supervisor = getCurrentAuthenticatedUser();

        Specification<Sale> spec = Specification.where(saleSpecs.withAgentesUnderSupervisor(supervisor.getId()))
                .and(saleSpecs.withFechaDesde(desde))
                .and(saleSpecs.withFechaHasta(hasta));

        List<Sale> ventas = saleRepository.findAll(spec);

        List<StatusCount> conteosPorEstado = buildConteosPorEstado(ventas);
        BigDecimal montoTotalAprobadas = buildMontoTotalAprobadas(ventas);
        List<SalesPerDay> ventasPorDia = buildVentasPorDia(ventas);

        ReportResponse response = new ReportResponse();
        response.setConteosPorEstado(conteosPorEstado);
        response.setMontoTotalAprobadas(montoTotalAprobadas);
        response.setVentasPorDia(ventasPorDia);
        return response;
    }

    private List<StatusCount> buildConteosPorEstado(List<Sale> ventas) {
        Map<SaleStatus, Long> counts = ventas.stream()
                .collect(Collectors.groupingBy(
                        Sale::getEstado,
                        Collectors.counting()
                ));

        return counts.entrySet().stream()
                .map(entry -> {
                    StatusCount estadoCount = new StatusCount();
                    estadoCount.setEstado(entry.getKey());
                    estadoCount.setCount(entry.getValue());
                    return estadoCount;
                })
                .collect(Collectors.toList());
    }

    private BigDecimal buildMontoTotalAprobadas(List<Sale> ventas) {
        return ventas.stream()
                .filter(s -> s.getEstado() == SaleStatus.APROBADA)
                .map(Sale::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<SalesPerDay> buildVentasPorDia(List<Sale> ventas) {
        Map<String, List<Sale>> byDay = ventas.stream()
                .collect(Collectors.groupingBy(
                        sale -> dateFormatter.format(sale.getFechaRegistro())
                ));

        return byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    SalesPerDay vd = new SalesPerDay();
                    vd.setFecha(entry.getKey());
                    vd.setCount((long) entry.getValue().size());
                    vd.setMonto(entry.getValue().stream()
                            .map(Sale::getMonto)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return vd;
                })
                .collect(Collectors.toList());
    }

    private User getCurrentAuthenticatedUser() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en BD"));
    }
}
