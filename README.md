# Telco – Flujo de Ventas Fija Hogar

Backend **Spring Boot 3 / Java 21** + frontend **React + Vite** que implementa el flujo de
ventas descrito en [`prueba-tecnica.md`](prueba-tecnica.md):

1. **Agente** registra una venta.
2. **Backoffice** aprueba o rechaza la venta (con motivo).
3. **Supervisor** consulta las ventas de su equipo con filtros y un resumen.

## Estructura

```
proyecto/
├── backend/    Spring Boot 3 (Java 21), PostgreSQL (Testcontainers en tests), JWT, OpenAPI
├── frontend/   React + Vite (consumidor de la API)
├── docs/       Documentación
├── schema.sql + data.sql  → ver backend/src/main/resources/ (entregables de BD)
└── prueba-tecnica.md      Enunciado de referencia
```

---

## Requisitos

- **Java 21** (JDK 21+)
- **Maven 3.9+** (o usar `./mvnw`)
- **Node.js 18+ / npm** (frontend)
- **PostgreSQL 14+** corriendo localmente (con la base y credenciales del `.env`)

---

## 1) Base de datos (PostgreSQL)

Los entregables de BD (`schema.sql` y `data.sql`) están en
`backend/src/main/resources/` y se ejecutan automáticamente al arrancar el backend
(`spring.sql.init.mode=always`). No se necesita ejecutarlos a mano, solo tener PostgreSQL
disponible.

1. Crea la base de datos (ajusta según tu `DB_USERNAME`/`DB_PASSWORD`):

   ```bash
   createdb -U postgres telco_db
   ```

2. Verifica que el puerto coincida con tu configuración (ver sección 2). Por defecto
   el backend espera PostgreSQL en **`localhost:5432`**.

> Nota: `schema.sql` incluye `DROP TABLE IF EXISTS` para facilitar reinicios en desarrollo;
> es un mecanismo dev-only exigido por la prueba técnica.

---

## 2) Backend (Spring Boot)

### 2.1 Configuración con `.env`

El backend lee las variables de entorno desde un archivo `.env` (via `spring-dotenv`) para
facilitar el arranque. **Copia el ejemplo y ajústalo a tu entorno:**

```bash
cd backend
cp .env.example .env
```

Valores disponibles en `.env.example`:

| Variable        | Por defecto                         | Descripción                            |
|-----------------|-------------------------------------|----------------------------------------|
| `DB_HOST`       | `localhost`                         | Host de PostgreSQL                     |
| `DB_PORT`       | `5432`                              | Puerto de PostgreSQL                   |
| `DB_USERNAME`   | `postgres`                          | Usuario de PostgreSQL                  |
| `DB_PASSWORD`   | `postgres`                          | Password de PostgreSQL                 |
| `DB_NAME`       | `telco_db`                          | Nombre de la base de datos             |
| `DB_URL`        | *(opcional)*                        | JDBC URL completo (anula los campos)   |
| `JWT_SECRET`    | `dev-secret-key-...`                | Secreto HMAC para firmar los JWT       |
| `JWT_EXPIRATION`| `3600000`                           | Expiración del token (ms)              |

> `.env` está en `.gitignore`; `.env.example` sí se versiona. Si tienes un PostgreSQL local
> en otro puerto (p. ej. `5433`), solo cambia `DB_PORT` en tu `.env`.

### 2.2 Arranque

```bash
cd backend
./mvnw spring-boot:run
```

La API queda en **`http://localhost:8080/api/v1`**.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 2.3 Tests

```bash
cd backend
./mvnw clean test        # usa PostgreSQL real en Docker (Testcontainers) para los tests de integración
```

> **Requisito:** Docker Desktop debe estar corriendo. Los tests de integración levantan un
> contenedor `postgres:16-alpine` automáticamente (no requiere una BD local configurada).

---

## 3) Frontend (React + Vite)

### 3.1 Configuración

```bash
cd frontend
cp .env.example .env
```

`VITE_API_BASE_URL` apunta a la API (por defecto `http://localhost:8080/api/v1`).

### 3.2 Arranque

```bash
cd frontend
npm install
npm run dev
```

El frontend queda en **`http://localhost:5173`** (habilitado en CORS).

---

## 4) Usuarios de prueba (seed)

| Usuario       | Password   | Rol          | Detalle                     |
|---------------|------------|--------------|-----------------------------|
| `agente1`     | `Agente*123`  | `AGENTE`     | Crea ventas, ve las suyas |
| `back1`       | `Back*123`   | `BACKOFFICE` | Ve pendientes, aprueba/rechaza |
| `supervisor1` | `Sup*123`    | `SUPERVISOR` | Ve ventas de su equipo + resumen |
| `admin`       | `Admin*123`  | `ADMIN`      | —                           |

> `agente1` y `agente2` están bajo la supervisión de `supervisor1`.

---

## 5) API (resumen)

Prefijo: `/api/v1`

| Método | Endpoint                  | Rol          | Descripción                          |
|--------|---------------------------|--------------|--------------------------------------|
| POST   | `/auth/login`             | público      | Login → JWT                          |
| POST   | `/ventas`                 | `AGENTE`     | Crear venta (PENDIENTE)              |
| GET    | `/ventas/mis-ventas`      | `AGENTE`     | Mis ventas (filtros + paginación)    |
| GET    | `/ventas/pendientes`      | `BACKOFFICE` | Ventas pendientes                    |
| POST   | `/ventas/{id}/aprobar`    | `BACKOFFICE` | Aprobar venta                        |
| POST   | `/ventas/{id}/rechazar`   | `BACKOFFICE` | Rechazar venta (requiere motivo)     |
| GET    | `/ventas/equipo`          | `SUPERVISOR` | Ventas del equipo (filtros)          |
| GET    | `/reportes/resumen`       | `SUPERVISOR` | Resumen por estado/montos/serie día  |

Errores JSON consistentes: `{ timestamp, path, error, message }`.

---

## 6) Documentación

- Swagger UI del backend: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI exportado: [`docs/openapi.yaml`](docs/openapi.yaml)
- Contrato para el frontend: [`docs/frontend/api-documentation.md`](docs/frontend/api-documentation.md)
- Plan de refactorización: [`plan.md`](plan.md)

> Para regenerar `docs/openapi.yaml` después de cambiar los controllers:
>
> ```bash
> cd backend
> mvn test -Dtest=OpenApiDumpTest -Djunit.jupiter.conditions.deactivate=org.junit.*DisabledCondition
> # el JSON queda en backend/target/openapi.json; convertir a YAML con el script de docs/ si existe
> ```
