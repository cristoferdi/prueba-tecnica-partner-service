# Contrato API – Frontend (Telco Ventas)

Documento de referencia del contrato HTTP/JSON que consume el frontend. La fuente
canónica es [`docs/openapi.yaml`](../openapi.yaml) (OpenAPI 3.1, exportado del backend).

## Convenciones generales

- **Base URL:** `http://localhost:8080/api/v1`
- **Formato de datos:** JSON (`Content-Type: application/json`).
- **Autenticación:** `Authorization: Bearer <token>` en cada petición salvo `/auth/login`.
  El token se obtiene de `/auth/login` y el rol real se resuelve desde la BD (el claim
  `role` del JWT es solo informativo).
- **Fechas:** `Instant` ISO-8601 con offset (p. ej. `2026-08-15T10:00:00-05:00`), ya sean
  `fechaRegistro`, `fechaValidacion`, `createdAt`, `updatedAt` o los filtros `desde`/`hasta`.

## Errores

Todos los errores usan el mismo shape `ApiError`:

```json
{
  "timestamp": "2026-08-15T10:00:00.000Z",
  "path": "/api/v1/ventas",
  "error": "Bad Request",
  "message": "mensaje legible",
  "fieldErrors": { "campo": "mensaje" }
}
```

- `fieldErrors` solo aparece en errores de validación (400).
- Códigos usados: `400` validación, `401` credenciales/token inválido, `403` sin permiso,
  `404` no encontrado, `409` `codigo_llamada` duplicado, `500` error inesperado.

## Endpoints

| Método | Endpoint                    | Rol          | Descripción                              |
|--------|-----------------------------|--------------|------------------------------------------|
| POST   | `/auth/login`               | público      | Login → `{ token, role }`                |
| POST   | `/ventas`                   | `AGENTE`     | Crear venta → 201 `SaleResponse`         |
| GET    | `/ventas/mis-ventas`        | `AGENTE`     | Mis ventas (filtros + paginación)        |
| GET    | `/ventas/pendientes`        | `BACKOFFICE` | Ventas PENDIENTES → `SaleResponse[]`     |
| POST   | `/ventas/{id}/aprobar`      | `BACKOFFICE` | Aprobar venta PENDIENTE → `SaleResponse` |
| POST   | `/ventas/{id}/rechazar`     | `BACKOFFICE` | Rechazar venta (motivo requerido)        |
| GET    | `/ventas/equipo`            | `SUPERVISOR` | Ventas del equipo → `SaleResponse[]`     |
| GET    | `/reportes/resumen`         | `SUPERVISOR` | `ReportResponse` (conteos/montos/serie)  |

### POST /auth/login

Body:

```json
{ "username": "agente1", "password": "Agente*123" }
```

Respuesta 200:

```json
{ "token": "<jwt>", "role": "AGENTE" }
```

### POST /ventas

Body (`SaleRequest`):

```json
{
  "dniCliente": "12345678",
  "nombreCliente": "Juan Pérez Ríos",
  "telefonoCliente": "987654321",
  "direccionCliente": "Av. Larco 123, Lima",
  "planActual": "Plan Básico",
  "planNuevo": "Plan Premium",
  "codigoLlamada": "CALL-20260815-0001",
  "producto": "Internet",
  "monto": 5000.0
}
```

Validaciones: `dniCliente` `\d{8}|\d{11}`, `telefonoCliente` `\d{9}`, `monto > 0`,
longitudes máximas según campo. Respuesta 201: `SaleResponse`.

### GET /ventas/mis-ventas

Query params: `estado` (PENDIENTE|APROBADA|RECHAZADA), `desde`, `hasta` (Instant ISO),
`page` (0-based), `size`, `sort`.

Respuesta 200 (`SalePageResponse`):

```json
{
  "content": [ /* SaleResponse */ ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "last": false,
  "first": true
}
```

### GET /ventas/equipo

Query params: `estado`, `agenteId`, `desde`, `hasta`. El supervisor solo ve ventas de sus
agentes; enviar un `agenteId` ajeno no amplía el alcance.

### POST /ventas/{id}/aprobar

Sin body. Requiere que la venta esté `PENDIENTE`. Respuesta 200: `SaleResponse` con
`estado=APROBADA` y `fechaValidacion` seteada.

### POST /ventas/{id}/rechazar

Body (`RejectSaleRequest`):

```json
{ "motivo": "Datos incompletos del cliente" }
```

Respuesta 200: `SaleResponse` con `estado=RECHAZADA`, `motivoRechazo` y `fechaValidacion`.

### GET /ventas/pendientes

Respuesta 200: `SaleResponse[]` (todas las PENDIENTES).

### GET /reportes/resumen

Query params: `desde`, `hasta` (opcionales).

Respuesta 200 (`ReportResponse`):

```json
{
  "conteosPorEstado": [
    { "estado": "PENDIENTE", "count": 5 },
    { "estado": "APROBADA", "count": 3 },
    { "estado": "RECHAZADA", "count": 2 }
  ],
  "montoTotalAprobadas": 15000.0,
  "ventasPorDia": [
    { "fecha": "2026-08-15", "count": 2, "monto": 10000.0 }
  ]
}
```

## Modelos

### SaleResponse

| Campo             | Tipo      | Descripción                        |
|-------------------|-----------|------------------------------------|
| `id`              | integer   | ID de la venta                     |
| `dniCliente`      | string    | DNI o RUC                          |
| `nombreCliente`   | string    | Nombre del cliente                 |
| `telefonoCliente` | string    | Teléfono                           |
| `direccionCliente`| string    | Dirección                          |
| `planActual`      | string    | Plan previo                        |
| `planNuevo`       | string    | Plan contratado                    |
| `codigoLlamada`   | string    | Código único de llamada (409 si duplica) |
| `producto`        | string    | Producto                           |
| `monto`           | number    | Monto                              |
| `estado`          | enum      | `PENDIENTE` / `APROBADA` / `RECHAZADA` |
| `motivoRechazo`   | string    | Solo en RECHAZADA                  |
| `fechaRegistro`   | Instant   | Fecha de registro                  |
| `fechaValidacion` | Instant   | Solo si APROBADA/RECHAZADA         |
| `agenteId`        | integer   | ID del agente                      |
| `agenteUsername`  | string    | Usuario del agente                 |
| `createdAt`       | Instant   | Fecha de creación                  |
| `updatedAt`       | Instant   | Fecha de última actualización      |

> Los estados de la BD son **mayúsculas** (`PENDIENTE`, `APROBADA`, `RECHAZADA`) y se
> devuelven tal cual en JSON.
