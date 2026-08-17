# Telco – Flujo de Ventas Fija Hogar

Solución Fullstack (Spring Boot 3 + React) para la gestión del flujo de ventas de telecomunicaciones.

---

## ⚙️ 1. Configuración Centralizada (.env)

El proyecto utiliza una configuración centralizada en la raíz para facilitar el despliegue. 

Crea tu archivo de variables de entorno copiando el ejemplo proporcionado en la **raíz del proyecto**:

```bash
# En Windows/Linux (desde la raíz del proyecto):
cp .env.example .env
```
> **Nota:** Los valores por defecto en el `.env` ya están configurados para funcionar "out-of-the-box" (PostgreSQL apuntando al puerto 5433, JWT y URL del frontend).

---

## 🗄️ 2. Levantar la Base de Datos (DB Incluida)

El proyecto incluye *seeding* automático. Tienes dos opciones para levantar PostgreSQL:

**Opción A: Vía Docker (Recomendada - Fricción Cero)**
Desde la raíz del proyecto, ejecuta:
```bash
docker compose up -d
```
*(Docker leerá automáticamente tu archivo `.env` en la raíz para levantar el motor con el puerto y credenciales correctas).*

**Opción B: Instalación Local**
Si no usas Docker, crea una base de datos local vacía llamada `telco_db` y asegúrate de que tus credenciales y puerto coincidan con tu archivo `.env`.

---

## 🚀 3. Ejecutar el Proyecto

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

Los scripts iniciales cargan **9 ventas variadas** (en estado PENDIENTE, APROBADA y RECHAZADA) y los siguientes usuarios. Úsalos en el Frontend para probar los distintos flujos:

| Rol | Usuario | Contraseña | Permisos / Notas |
|---|---|---|---|
| **Admin** | `admin` | `Admin*123` | Acceso global del sistema. |
| **Supervisor** | `supervisor1` | `Sup*123` | Ve métricas y listado de ventas de sus agentes a cargo. |
| **Agente** | `agente1` | `Agente*123` | Registra ventas y visualiza sus registros (Bajo supervisión de `supervisor1`). |
| **Agente** | `agente2` | `Agente2*123` | Registra ventas y visualiza sus registros (Bajo supervisión de `supervisor1`). |
| **Backoffice** | `back1` | `Back*123` | Ve ventas PENDIENTES, aprueba o rechaza (exigiendo motivo). |

---

## 📚 5. Entregables y Documentación

El repositorio cumple con todos los entregables solicitados en la prueba técnica:

- **Código Fuente:** Carpetas separadas para `backend/` y `frontend/`.
- **Scripts BD (`schema.sql` y `data.sql`):** Ubicados en `backend/src/main/resources/`. Se encargan de crear la estructura y poblar los usuarios y las ventas iniciales automáticamente al arrancar el backend.
- **Contrato y Documentación API (OpenAPI):** 
  - Archivo exportado: `openapi.yaml` (En la raíz del proyecto).
  - Interfaz interactiva (Swagger UI): [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) *(Requiere tener el backend en ejecución)*.
- **Documentación Técnica (Carpeta `docs/`):**
  - Diagrama simple de la solución: `docs/diagrama_solucion.png`
  - Decisiones técnicas y despliegue: `docs/decisiones_tecnicas.md`
- **Calidad (Tests de Integración):** Suite de pruebas validando el flujo y la base de datos usando Testcontainers con PostgreSQL real. Ejecutables vía `cd backend && ./mvnw clean test`.