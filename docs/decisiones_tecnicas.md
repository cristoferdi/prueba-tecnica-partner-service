# Decisiones Técnicas y Guía de Despliegue Local

Documento de cierre del proyecto **Telco – Flujo de Ventas Fija Hogar** (backend Spring Boot 3 / Java 21 + frontend React/Vite).

---

## 1. Decisiones Técnicas Destacadas

- **Arquitectura Limpia (Controller-Service-Repository) y DDD:** Se refactorizó la estructura hacia principios de Domain-Driven Design. La lógica de transición de estado se encapsuló en el dominio (`Sale.approve()`, `Sale.reject()`), dejando a los servicios como orquestadores delgados.
- **Persistencia, Transacciones y Optimización en PostgreSQL:** Se cumplió el requerimiento de guardar el *snapshot* de los datos del cliente directamente en la entidad `Venta`. Para los reportes, se delegaron las agrupaciones y conteos a la base de datos mediante consultas JPQL optimizadas, evitando sobrecargar la memoria del servidor. Se hizo uso de transacciones (`@Transactional`) para garantizar la integridad de los datos durante las aprobaciones y rechazos.
- **Manejo de Constraints, Validaciones y Errores RESTful:** Se implementaron validaciones estrictas de entrada (DNI de 8/11 dígitos, teléfono de 9, campos obligatorios). Se previno la condición de carrera en la creación de ventas manejando la restricción de base de datos (`uk_venta_codigo_llamada`), retornando un HTTP 409 (Conflict). Todos los errores se centralizan en un `GlobalExceptionHandler` devolviendo un JSON uniforme (`{timestamp, path, error, message}`), incluso en excepciones de seguridad.
- **Calidad de Software (Testcontainers e Integración):** Se descartó H2 en favor de Testcontainers con una imagen real de PostgreSQL 16 para las pruebas. Esto garantiza que las pruebas se ejecuten en un motor idéntico al de producción, validando el SQL nativo, la inferencia de tipos y eliminando falsos positivos. La suite finaliza con 109 tests en verde.
- **Seguridad y JWT (Roles y Permisos):** Se diseñó una estrategia de seguridad donde el rol y el `supervisor_id` siempre se resuelven consultando la base de datos (`CustomUserDetails`). El token JWT se mantiene estático como mecanismo de transporte, pero la autorización real es validada en el servidor, evitando escalamiento de privilegios desde el cliente.
- **API Versionada y Autodocumentada:** Se implementó una API RESTful bajo el prefijo `/api/v1`. La documentación técnica de los endpoints se genera dinámicamente con OpenAPI 3 y Springdoc, lo que permite la exportación directa a Postman o la interacción mediante Swagger UI.
- **Gestión de Entorno Centralizada (Twelve-Factor App):** Se unificó la configuración de Docker, Spring Boot y Vite en un único archivo `.env` en la raíz del proyecto. Esto elimina la duplicidad de variables, facilita la inyección de secretos en producción y mejora notablemente la Experiencia del Desarrollador (DX) durante el despliegue local.

---

## 2. Guía de Despliegue Local

**1. Configuración del Entorno.** 
- En la raíz del proyecto, ejecutar `cp .env.example .env`. 
- *(Tanto Docker como el Backend y Frontend están configurados para leer de este único archivo central).*

**2. Base de datos (PostgreSQL).** 
- *Opción A (Docker):* Ejecutar `docker compose up -d` en la raíz para levantar PostgreSQL. 
- *Opción B (Local):* Crear la base de datos `telco_db` manualmente. 
*Nota: La estructura (schema.sql) y el seeding (data.sql) se ejecutan automáticamente al arrancar el backend.*

**3. Backend (Spring Boot).** 
- Ir a `backend/` → `./mvnw spring-boot:run`. 
- La API responderá en `http://localhost:8080/api/v1` y Swagger UI en `http://localhost:8080/swagger-ui/index.html`.

**4. Frontend (React/Vite).** 
- Ir a `frontend/` → `npm install` → `npm run dev`. 
- La aplicación se levanta en `http://localhost:5173`.

**5. Tests de Integración.** 
- Ir a `backend/` → `./mvnw clean test`. 
- *(Requiere Docker en ejecución para que Testcontainers levante la base de datos efímera).*

**6. Usuarios Seed.** 
- Admin: `admin / Admin*123`
- Backoffice: `back1 / Back*123`
- Supervisor: `supervisor1 / Sup*123`
- Agente (equipo supervisor1): `agente1 / Agente*123`