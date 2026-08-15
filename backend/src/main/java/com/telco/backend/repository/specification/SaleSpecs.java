package com.telco.backend.repository.specification;

import com.telco.backend.domain.Sale;
import com.telco.backend.domain.SaleStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SaleSpecs {

    public static final String ATTR_AGENTE = "agente";
    public static final String ATTR_SUPERVISOR = "supervisor";
    public static final String ATTR_ESTADO = "estado";
    public static final String ATTR_FECHA_REGISTRO = "fechaRegistro";

    public Specification<Sale> withAgenteId(Long agenteId) {
        return (root, query, cb) ->
                agenteId != null
                        ? cb.equal(root.get(ATTR_AGENTE).get("id"), agenteId)
                        : null;
    }

    public Specification<Sale> withEstado(SaleStatus estado) {
        return (root, query, cb) ->
                estado != null
                        ? cb.equal(root.get(ATTR_ESTADO), estado)
                        : null;
    }

    public Specification<Sale> withFechaDesde(Instant desde) {
        return (root, query, cb) ->
                desde != null
                        ? cb.greaterThanOrEqualTo(root.get(ATTR_FECHA_REGISTRO), desde)
                        : null;
    }

    public Specification<Sale> withFechaHasta(Instant hasta) {
        return (root, query, cb) ->
                hasta != null
                        ? cb.lessThanOrEqualTo(root.get(ATTR_FECHA_REGISTRO), hasta)
                        : null;
    }

    public Specification<Sale> withAgentesUnderSupervisor(Long supervisorId) {
        return (root, query, cb) ->
                cb.equal(root.get(ATTR_AGENTE).get(ATTR_SUPERVISOR).get("id"), supervisorId);
    }
}
