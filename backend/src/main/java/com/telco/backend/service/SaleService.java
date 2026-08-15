package com.telco.backend.service;

import com.telco.backend.domain.Sale;
import com.telco.backend.domain.SaleStatus;
import com.telco.backend.domain.User;
import com.telco.backend.domain.exception.DuplicateCallCodeException;
import com.telco.backend.domain.exception.SaleNotFoundException;
import com.telco.backend.repository.SaleRepository;
import com.telco.backend.repository.specification.SaleSpecs;
import com.telco.backend.web.dto.SaleFilter;
import com.telco.backend.web.dto.SalePageResponse;
import com.telco.backend.web.dto.SaleRequest;
import com.telco.backend.web.dto.SaleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleSpecs saleSpecs;
    private final CurrentUserService currentUserService;
    private final SaleMapper saleMapper;

    public SaleResponse createSale(SaleRequest request) {
        if (saleRepository.existsByCodigoLlamada(request.getCodigoLlamada())) {
            throw new DuplicateCallCodeException(request.getCodigoLlamada());
        }

        User agente = currentUserService.getCurrentAuthenticatedUser();

        Sale sale = Sale.newPending(
                agente,
                request.getDniCliente(),
                request.getNombreCliente(),
                request.getTelefonoCliente(),
                request.getDireccionCliente(),
                request.getPlanActual(),
                request.getPlanNuevo(),
                request.getCodigoLlamada(),
                request.getProducto(),
                request.getMonto()
        );

        Sale savedSale;
        try {
            savedSale = saleRepository.save(sale);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateCallCodeException(request.getCodigoLlamada());
        }

        return saleMapper.toResponse(savedSale);
    }

    public SalePageResponse getMisVentas(SaleFilter filter, Pageable pageable) {
        User agente = currentUserService.getCurrentAuthenticatedUser();

        Page<Sale> page = saleRepository.findAll(
                saleSpecs.withAgenteId(agente.getId())
                        .and(saleSpecs.withEstado(filter.getEstado()))
                        .and(saleSpecs.withFechaDesde(filter.getDesde()))
                        .and(saleSpecs.withFechaHasta(filter.getHasta())),
                pageable
        );

        List<SaleResponse> content = page.getContent().stream()
                .map(saleMapper::toResponse)
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
        User supervisor = currentUserService.getCurrentAuthenticatedUser();

        Specification<Sale> spec = Specification.where(saleSpecs.withAgentesUnderSupervisor(supervisor.getId()))
                .and(saleSpecs.withEstado(estado))
                .and(saleSpecs.withAgenteId(agenteId))
                .and(saleSpecs.withFechaDesde(desde))
                .and(saleSpecs.withFechaHasta(hasta));

        return saleRepository.findAll(spec).stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<SaleResponse> getPendingSales() {
        return saleRepository.findAll(
                Specification.where(saleSpecs.withEstado(SaleStatus.PENDIENTE))
        ).stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }

    public SaleResponse approveSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new SaleNotFoundException(id));

        sale.approve();

        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toResponse(savedSale);
    }

    public SaleResponse rejectSale(Long id, String motivo) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new SaleNotFoundException(id));

        sale.reject(motivo);

        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toResponse(savedSale);
    }
}