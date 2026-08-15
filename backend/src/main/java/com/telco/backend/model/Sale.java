package com.telco.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

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
}
