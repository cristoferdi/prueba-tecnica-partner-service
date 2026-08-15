-- =============================================================================
-- Telco Backend - Database Schema
-- =============================================================================
-- ENTREGABLE REQUERIDO por prueba-tecnica.md.
-- Se ejecuta automáticamente al arrancar (spring.sql.init.mode=always).
--
-- ADVERTENCIA (dev-only): los DROP TABLE fuerzan un reinicio limpio de la BD en
-- cada arranque. Solo es un mecanismo de seed/desarrollo exigido por el enunciado.
-- =============================================================================

DROP TABLE IF EXISTS venta;
DROP TABLE IF EXISTS usuario;

-- =============================================================================
-- Table: usuario
-- =============================================================================

CREATE TABLE usuario (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol           VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN', 'AGENTE', 'BACKOFFICE', 'SUPERVISOR')),
    supervisor_id BIGINT,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_supervisor
        FOREIGN KEY (supervisor_id)
        REFERENCES usuario (id)
        ON DELETE SET NULL
);

-- =============================================================================
-- Table: venta
-- =============================================================================

CREATE TABLE venta (
    id                BIGSERIAL PRIMARY KEY,
    agente_id         BIGINT           NOT NULL,
    dni_cliente       VARCHAR(20)      NOT NULL,
    nombre_cliente    VARCHAR(100)     NOT NULL,
    telefono_cliente  VARCHAR(20)      NOT NULL,
    direccion_cliente VARCHAR(200)     NOT NULL,
    plan_actual       VARCHAR(50)      NOT NULL,
    plan_nuevo        VARCHAR(50)      NOT NULL,
    codigo_llamada    VARCHAR(50)      NOT NULL,
    producto          VARCHAR(100)     NOT NULL,
    monto             DECIMAL(10,2)    NOT NULL,
    estado            VARCHAR(20)      NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')),
    motivo_rechazo    VARCHAR(255),
    fecha_registro    TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_validacion  TIMESTAMP WITH TIME ZONE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_venta_agente
        FOREIGN KEY (agente_id)
        REFERENCES usuario (id)
        ON DELETE RESTRICT,
    CONSTRAINT uk_venta_codigo_llamada
        UNIQUE (codigo_llamada)
);

-- =============================================================================
-- Indexes
-- =============================================================================

CREATE INDEX idx_venta_estado         ON venta(estado);
CREATE INDEX idx_venta_fecha_registro ON venta(fecha_registro);
CREATE INDEX idx_venta_agente         ON venta(agente_id);
CREATE INDEX idx_usuario_supervisor   ON usuario(supervisor_id);
