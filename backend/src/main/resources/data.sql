-- =============================================================================
-- Telco Backend - Seed Data
-- =============================================================================
-- Passwords are hashed with BCrypt (cost factor 10).
-- Seeds:
--   admin       / Admin*123
--   agente1     / Agente*123
--   agente2     / Agente2*123
--   back1       / Back*123
--   supervisor1 / Sup*123
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Users
-- ---------------------------------------------------------------------------

INSERT INTO usuario (id, username, password_hash, rol, activo) VALUES
(1, 'supervisor1', '$2b$10$FHfqOAFBYkNG7vMiPlekrO8GVQpxvemnnuMh2Udbr8T1MSeQLXReO', 'SUPERVISOR', true),
(2, 'admin',       '$2b$10$ULVyHQhNa37p7nT.SJ7aveUNS3Sa1vgjXoo2PuF2QbOaWEx9qWzMC', 'ADMIN',      true),
(3, 'back1',       '$2b$10$wVyRPLvDugHLuMG4iw4mE.fSxKUBwoQlABt9Xc3MbRIDmkSyaToP2', 'BACKOFFICE', true),
(4, 'agente1',     '$2b$10$IWDX2EfFKaFZwPkdzcSASuPmu/bmNSBlPDEOWPECTSbJPCI0og4Ci', 'AGENTE',     true),
(5, 'agente2',     '$2b$10$44qN7/tt0iU2T/.B5aFuveXhRTZBSdtvSA3AxSnBqIZI3ruoTEhMy', 'AGENTE',     true),
(6, 'inactive1',   '$2b$10$IWDX2EfFKaFZwPkdzcSASuPmu/bmNSBlPDEOWPECTSbJPCI0og4Ci', 'AGENTE',     false);

-- Assign supervisors to agents
UPDATE usuario SET supervisor_id = 1 WHERE id = 4;
UPDATE usuario SET supervisor_id = 1 WHERE id = 5;

-- ---------------------------------------------------------------------------
-- Sales
-- Varied set covering all states, different agents, rejection reasons,
-- amounts, and dates to support future filtering and reporting tests.
-- DNI: 8 digits | Teléfono: 9 digits | Contexto Perú
-- ---------------------------------------------------------------------------

INSERT INTO venta (id, agente_id, dni_cliente, nombre_cliente, telefono_cliente, direccion_cliente, plan_actual, plan_nuevo, codigo_llamada, producto, monto, estado, motivo_rechazo, fecha_registro, fecha_validacion) VALUES
-- 1 – PENDIENTE, agente1, monto alto
(1, 4, '12345678', 'Juan Pérez Ríos',     '987654321', 'Av. Larco 123, Lima',                     'Plan Básico',    'Plan Premium',    'CALL-20250115-0001', 'Internet',     5000.00, 'PENDIENTE',  NULL,                                    '2025-01-15 10:30:00-05', NULL),
-- 2 – APROBADA, agente2, monto medio
(2, 5, '23456789', 'María Quispe Chau',   '912345678', 'Jr. San Martín 456, Arequipa',            'Plan Premium',   'Plan Empresarial','CALL-20250110-0002', 'Fibra Óptica',  3500.00, 'APROBADA',   NULL,                                    '2025-01-10 09:00:00-05', '2025-01-11 14:00:00-05'),
-- 3 – RECHAZADA, agente1, motivo DNI no verificable
(3, 4, '87654321', 'Carlos Valdez Soto',  '911234567', 'Av. Arequipa 789, Lima',                   'Plan Básico',    'Plan Premium',    'CALL-20250112-0003', 'Internet',     2000.00, 'RECHAZADA',  'DNI no verificable',                      '2025-01-12 11:00:00-05', '2025-01-13 10:00:00-05'),
-- 4 – PENDIENTE, agente2, monto alto
(4, 5, '99999999', 'Ana Quispe Ríos',     '923456789', 'Av. Universitaria 101, Trujillo',          'Plan Básico',    'Plan Premium',    'CALL-20250118-0004', 'Internet',     7500.00, 'PENDIENTE',  NULL,                                    '2025-01-18 16:00:00-05', NULL),
-- 5 – APROBADA, agente1, monto bajo
(5, 4, '45678901', 'Luis Ríos Mendoza',   '934567890', 'Jr. Grau 234, Cusco',                     'Plan Básico',    'Plan Premium',    'CALL-20250108-0005', 'Internet',     1500.00, 'APROBADA',   NULL,                                    '2025-01-08 08:00:00-05', '2025-01-09 12:00:00-05'),
-- 6 – RECHAZADA, agente1, motivo deuda pendiente
(6, 4, '55667788', 'Sofía Vásquez López', '945678901', 'Av. Benavides 345, Lima',                  'Plan Premium',   'Plan Empresarial','CALL-20250114-0006', 'Fibra Óptica',  4000.00, 'RECHAZADA',  'Cliente con deuda pendiente',             '2025-01-14 13:00:00-05', '2025-01-15 09:00:00-05'),
-- 7 – PENDIENTE, agente2, monto medio
(7, 5, '66778899', 'Roberto Díaz Paredes', '956789012', 'Jr. Amazonas 567, Chiclayo',               'Plan Básico',    'Plan Premium',    'CALL-20250120-0007', 'Internet',     6000.00, 'PENDIENTE',  NULL,                                    '2025-01-20 15:00:00-05', NULL),
-- 8 – APROBADA, agente2, monto alto
(8, 5, '77889900', 'Claudia Paredes Ruiz', '967890123', 'Av. Larco 890, Lima',                      'Plan Premium',   'Plan Empresarial','CALL-20250105-0008', 'Fibra Óptica',  9000.00, 'APROBADA',   NULL,                                    '2025-01-05 10:00:00-05', '2025-01-06 11:00:00-05'),
-- 9 – PENDIENTE, agente1 (additional seed for variety)
(9, 4, '33445566', 'Miguel Torres Silva',  '978901234', 'Calle 18 # 395, Lima',                     'Plan Básico',    'Plan Premium',    'CALL-20250122-0009', 'Internet',      3000.00, 'PENDIENTE',  NULL,                                    '2025-01-22 09:15:00-05', NULL);

-- ---------------------------------------------------------------------------
-- Sync sequences with explicit IDs used above.
-- This prevents ID conflicts when JPA inserts new rows via IDENTITY generation.
-- ---------------------------------------------------------------------------

SELECT setval('usuario_id_seq', GREATEST((SELECT MAX(id) FROM usuario), 1));
SELECT setval('venta_id_seq',   GREATEST((SELECT MAX(id) FROM venta), 1));