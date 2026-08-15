package com.telco.backend.venta.specification;

import com.telco.backend.model.Sale;
import com.telco.backend.model.SaleStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SaleSpecs {

    public Specification<Sale> withAgenteId(Long agenteId) {
        return (root, query, cb) ->
                agenteId != null
                        ? cb.equal(root.get("agente").get("id"), agenteId)
                        : null;
    }

    public Specification<Sale> withEstado(SaleStatus estado) {
        return (root, query, cb) ->
                estado != null
                        ? cb.equal(root.get("estado"), estado)
                        : null;
    }

    public Specification<Sale> withFechaDesde(Instant desde) {
        return (root, query, cb) ->
                desde != null
                        ? cb.greaterThanOrEqualTo(root.get("fechaRegistro"), desde)
                        : null;
    }

    public Specification<Sale> withFechaHasta(Instant hasta) {
        return (root, query, cb) ->
                hasta != null
                        ? cb.lessThanOrEqualTo(root.get("fechaRegistro"), hasta)
                        : null;
    }

    public Specification<Sale> withAgentesUnderSupervisor(Long supervisorId) {
        return (root, query, cb) ->
                cb.equal(root.get("agente").get("supervisor").get("id"), supervisorId);
    }
}
