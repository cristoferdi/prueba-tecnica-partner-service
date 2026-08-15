package com.telco.backend.domain;

import com.telco.backend.domain.exception.SaleNotPendingException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Agregado Venta.
 * <p>
 * Invariantes:
 * <ul>
 *   <li>Una venta nueva nace siempre en {@link SaleStatus#PENDIENTE} con {@code fechaRegistro} fijado.</li>
 *   <li>{@code estado} solo transiciona desde PENDIENTE hacia APROBADA o RECHAZADA.</li>
 *   <li>Al aprobar: {@code estado = APROBADA} y {@code fechaValidacion} se setea; {@code motivoRechazo} queda null.</li>
 *   <li>Al rechazar: {@code estado = RECHAZADA}, {@code motivoRechazo} es obligatorio y {@code fechaValidacion} se setea.</li>
 *   <li>{@code codigoLlamada} es único (restricción de BD).</li>
 * </ul>
 */
@Getter
@Setter
@Entity
@Table(name = "venta")
@NoArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id", nullable = false)
    private User agente;

    @Column(name = "dni_cliente", length = 20, nullable = false)
    private String dniCliente;

    @Column(name = "nombre_cliente", length = 100, nullable = false)
    private String nombreCliente;

    @Column(name = "telefono_cliente", length = 20, nullable = false)
    private String telefonoCliente;

    @Column(name = "direccion_cliente", length = 200, nullable = false)
    private String direccionCliente;

    @Column(name = "plan_actual", length = 50, nullable = false)
    private String planActual;

    @Column(name = "plan_nuevo", length = 50, nullable = false)
    private String planNuevo;

    @Column(name = "codigo_llamada", length = 50, nullable = false, unique = true)
    private String codigoLlamada;

    @Column(name = "producto", length = 100, nullable = false)
    private String producto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private SaleStatus estado = SaleStatus.PENDIENTE;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro;

    @Column(name = "fecha_validacion")
    private Instant fechaValidacion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Factory para crear una venta nueva en estado PENDIENTE con fecha de registro actual.
     */
    public static Sale newPending(User agente,
                                  String dniCliente,
                                  String nombreCliente,
                                  String telefonoCliente,
                                  String direccionCliente,
                                  String planActual,
                                  String planNuevo,
                                  String codigoLlamada,
                                  String producto,
                                  BigDecimal monto) {
        Sale sale = new Sale();
        sale.setAgente(agente);
        sale.setDniCliente(dniCliente);
        sale.setNombreCliente(nombreCliente);
        sale.setTelefonoCliente(telefonoCliente);
        sale.setDireccionCliente(direccionCliente);
        sale.setPlanActual(planActual);
        sale.setPlanNuevo(planNuevo);
        sale.setCodigoLlamada(codigoLlamada);
        sale.setProducto(producto);
        sale.setMonto(monto);
        sale.setEstado(SaleStatus.PENDIENTE);
        sale.setFechaRegistro(Instant.now());
        return sale;
    }

    /**
     * Aprueba la venta. Solo válido si está en estado PENDIENTE.
     */
    public void approve() {
        requirePending();
        this.estado = SaleStatus.APROBADA;
        this.motivoRechazo = null;
        this.fechaValidacion = Instant.now();
    }

    /**
     * Rechaza la venta con un motivo. Solo válido si está en estado PENDIENTE.
     */
    public void reject(String motivo) {
        requirePending();
        this.estado = SaleStatus.RECHAZADA;
        this.motivoRechazo = motivo;
        this.fechaValidacion = Instant.now();
    }

    private void requirePending() {
        if (estado != SaleStatus.PENDIENTE) {
            throw new SaleNotPendingException(id, estado.name());
        }
    }
}
