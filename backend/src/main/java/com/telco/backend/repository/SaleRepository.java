package com.telco.backend.repository;

import com.telco.backend.domain.Sale;
import com.telco.backend.domain.SaleStatus;
import com.telco.backend.web.dto.SalesPerDay;
import com.telco.backend.web.dto.StatusCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {

    boolean existsByCodigoLlamada(String codigoLlamada);

    @Query("""
            SELECT new com.telco.backend.web.dto.StatusCount(s.estado, COUNT(s))
            FROM Sale s
            WHERE s.agente.supervisor.id = :supervisorId
              AND (:desde IS NULL OR s.fechaRegistro >= :desde)
              AND (:hasta IS NULL OR s.fechaRegistro <= :hasta)
            GROUP BY s.estado
            """)
    List<StatusCount> countByEstadoForSupervisor(@Param("supervisorId") Long supervisorId,
                                                 @Param("desde") Instant desde,
                                                 @Param("hasta") Instant hasta);

    @Query("""
            SELECT COALESCE(SUM(s.monto), 0)
            FROM Sale s
            WHERE s.agente.supervisor.id = :supervisorId
              AND s.estado = :estado
              AND (:desde IS NULL OR s.fechaRegistro >= :desde)
              AND (:hasta IS NULL OR s.fechaRegistro <= :hasta)
            """)
    BigDecimal sumMontoByEstadoForSupervisor(@Param("supervisorId") Long supervisorId,
                                             @Param("estado") SaleStatus estado,
                                             @Param("desde") Instant desde,
                                             @Param("hasta") Instant hasta);

    @Query("""
            SELECT new com.telco.backend.web.dto.SalesPerDay(
                CAST(s.fechaRegistro AS date),
                COUNT(s),
                COALESCE(SUM(s.monto), 0))
            FROM Sale s
            WHERE s.agente.supervisor.id = :supervisorId
              AND (:desde IS NULL OR s.fechaRegistro >= :desde)
              AND (:hasta IS NULL OR s.fechaRegistro <= :hasta)
            GROUP BY CAST(s.fechaRegistro AS date)
            ORDER BY CAST(s.fechaRegistro AS date)
            """)
    List<SalesPerDay> seriesPorDiaForSupervisor(@Param("supervisorId") Long supervisorId,
                                                @Param("desde") Instant desde,
                                                @Param("hasta") Instant hasta);
}
