package com.telco.backend.venta;

import com.telco.backend.model.Sale;
import com.telco.backend.model.SaleStatus;
import com.telco.backend.model.User;
import com.telco.backend.repository.SaleRepository;
import com.telco.backend.repository.UserRepository;
import com.telco.backend.security.user.CustomUserDetails;
import com.telco.backend.venta.dto.SaleFilter;
import com.telco.backend.venta.dto.SalePageResponse;
import com.telco.backend.venta.dto.SaleRequest;
import com.telco.backend.venta.dto.SaleResponse;
import com.telco.backend.venta.dto.RechazarVentaRequest;
import com.telco.backend.venta.exception.DuplicateCodigoLlamadaException;
import com.telco.backend.venta.exception.VentaNotFoundException;
import com.telco.backend.venta.exception.VentaNotPendingException;
import com.telco.backend.venta.specification.SaleSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final SaleSpecs saleSpecs;

    public SaleResponse createSale(SaleRequest request) {
        if (saleRepository.existsByCodigoLlamada(request.getCodigoLlamada())) {
            throw new DuplicateCodigoLlamadaException(request.getCodigoLlamada());
        }

        User agente = getCurrentAuthenticatedUser();

        Sale sale = new Sale();
        sale.setAgente(agente);
        sale.setDniCliente(request.getDniCliente());
        sale.setNombreCliente(request.getNombreCliente());
        sale.setTelefonoCliente(request.getTelefonoCliente());
        sale.setDireccionCliente(request.getDireccionCliente());
        sale.setPlanActual(request.getPlanActual());
        sale.setPlanNuevo(request.getPlanNuevo());
        sale.setCodigoLlamada(request.getCodigoLlamada());
        sale.setProducto(request.getProducto());
        sale.setMonto(request.getMonto());
        sale.setEstado(SaleStatus.PENDIENTE);
        sale.setFechaRegistro(Instant.now());

        Sale savedSale = saleRepository.save(sale);

        return toResponse(savedSale);
    }

    public SalePageResponse getMisVentas(SaleFilter filter, Pageable pageable) {
        User agente = getCurrentAuthenticatedUser();

        Page<Sale> page = saleRepository.findAll(
                saleSpecs.withAgenteId(agente.getId())
                        .and(saleSpecs.withEstado(filter.getEstado()))
                        .and(saleSpecs.withFechaDesde(filter.getDesde()))
                        .and(saleSpecs.withFechaHasta(filter.getHasta())),
                pageable
        );

        List<SaleResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        SalePageResponse response = new SalePageResponse();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());
        response.setFirst(page.isFirst());
        return response;
    }

    public List<SaleResponse> getEquipoVentas(SaleStatus estado, Long agenteId, Instant desde, Instant hasta) {
        User supervisor = getCurrentAuthenticatedUser();

        Specification<Sale> spec = Specification.where(saleSpecs.withAgentesUnderSupervisor(supervisor.getId()))
                .and(saleSpecs.withEstado(estado))
                .and(saleSpecs.withAgenteId(agenteId))
                .and(saleSpecs.withFechaDesde(desde))
                .and(saleSpecs.withFechaHasta(hasta));

        return saleRepository.findAll(spec).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<SaleResponse> getPendingSales() {
        return saleRepository.findAll(
                Specification.where(saleSpecs.withEstado(SaleStatus.PENDIENTE))
        ).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SaleResponse approveSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new VentaNotFoundException(id));

        if (sale.getEstado() != SaleStatus.PENDIENTE) {
            throw new VentaNotPendingException(id, sale.getEstado().name());
        }

        sale.setEstado(SaleStatus.APROBADA);
        sale.setFechaValidacion(Instant.now());

        Sale savedSale = saleRepository.save(sale);
        return toResponse(savedSale);
    }

    public SaleResponse rejectSale(Long id, String motivo) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new VentaNotFoundException(id));

        if (sale.getEstado() != SaleStatus.PENDIENTE) {
            throw new VentaNotPendingException(id, sale.getEstado().name());
        }

        sale.setEstado(SaleStatus.RECHAZADA);
        sale.setMotivoRechazo(motivo);
        sale.setFechaValidacion(Instant.now());

        Sale savedSale = saleRepository.save(sale);
        return toResponse(savedSale);
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en BD"));
    }

    private SaleResponse toResponse(Sale sale) {
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
        if (sale.getAgente() != null) {
            response.setAgenteId(sale.getAgente().getId());
            response.setAgenteUsername(sale.getAgente().getUsername());
        }
        if (sale.getCreatedAt() != null) {
            response.setCreatedAt(sale.getCreatedAt().toString());
        }
        if (sale.getUpdatedAt() != null) {
            response.setUpdatedAt(sale.getUpdatedAt().toString());
        }
        return response;
    }
}