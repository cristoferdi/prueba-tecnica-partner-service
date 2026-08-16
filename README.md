# Telco – Flujo de Ventas Fija Hogar

Solución Fullstack (Spring Boot 3 + React) para la gestión del flujo de ventas de telecomunicaciones.

---

## ⚙️ 1. Levantar la Base de Datos (DB Incluida)

El proyecto incluye *seeding* automático (`schema.sql` y `data.sql`). Tienes dos opciones para levantar PostgreSQL en el puerto `5432`:

**Opción A: Vía Docker (Recomendada - Fricción Cero)**
```bash
docker compose up -d
```
*(Levanta el motor con las credenciales por defecto `postgres / postgres`).*

**Opción B: Instalación Local**
Si no usas Docker, simplemente crea una base de datos local vacía llamada `telco_db` y asegúrate de que tus credenciales coincidan con el archivo `.env`.

---

## 🔐 2. Variables de Entorno

Tanto en la carpeta `backend/` como en `frontend/` existe un archivo `.env.example`.
Renómbralos a `.env` antes de ejecutar el proyecto:

```bash
# En Windows/Linux:
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
```
> **Nota:** Los valores por defecto en los `.env.example` ya están configurados para funcionar "out-of-the-box" con la base de datos de Docker y los puertos por defecto.

---

## 🚀 3. Ejecutar el Proyecto (Windows / Linux)

### Backend (API en puerto 8080)
Requiere Java 21. Al arrancar, poblará la base de datos automáticamente.
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend (SPA en puerto 5173)
Requiere Node.js 18+.
```bash
cd frontend
npm install
npm run dev
```

---

## 👥 4. Usuarios de Prueba (Seed)

Usa estas credenciales en el Frontend para probar los distintos roles del flujo:

| Rol | Usuario | Contraseña | Permisos principales |
|---|---|---|---|
| **Agente** | `agente1` | `Agente*123` | Registra ventas y visualiza sus registros. |
| **Backoffice** | `back1` | `Back*123` | Ve ventas PENDIENTES, aprueba o rechaza (con motivo). |
| **Supervisor** | `supervisor1` | `Sup*123` | Ve métricas y listado de ventas de `agente1`. |
| **Admin** | `admin` | `Admin*123` | Acceso global. |

---

## 📚 5. Documentación y Entregables

Toda la documentación técnica y de API solicitada se encuentra disponible en:

- **Diagrama de Solución y Arquitectura:** `docs/diagrama_solucion.png`
- **Decisiones Técnicas:** `docs/decisiones_tecnicas.md`
- **Documentación API (Swagger UI):** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) *(Con el backend corriendo)*
- **Colección OpenAPI / Postman:** `openapi.yaml` *(En la raíz del proyecto)*
- **Tests de Integración:** Ejecutables vía `cd backend && ./mvnw clean test` (Testcontainers con PostgreSQL real).