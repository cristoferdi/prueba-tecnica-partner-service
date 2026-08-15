package com.telco.backend.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base para los tests de integración: arranca un PostgreSQL real en Docker
 * (Testcontainers) y conecta la aplicación al contenedor de forma automática
 * vía {@link ServiceConnection}.
 *
 * <p>El contenedor se arranca una única vez (bloque {@code static}) y se
 * comparte entre todas las clases que heredan esta base, por lo que el
 * contexto Spring se cachea con una URL de conexión estable.
 *
 * <p>Si Docker no está disponible, los tests fallan al intentar arrancar el
 * contenedor (el entorno de CI debe garantizar Docker).
 */
@ActiveProfiles("test")
public abstract class AbstractPostgresContainerTest {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("telco_test")
            .withUsername("test")
            .withPassword("test")
            .withEnv("TZ", "UTC");

    static {
        POSTGRES.start();
    }

    @ServiceConnection
    static PostgreSQLContainer<?> postgres() {
        return POSTGRES;
    }

    protected AbstractPostgresContainerTest() {
    }
}
