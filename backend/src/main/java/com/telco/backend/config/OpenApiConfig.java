package com.telco.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI telcoOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Telco Backend API")
                        .version("1.0.0")
                        .description("""
                                API del flujo de ventas Telco Fija Hogar.

                                Autenticación: usa el endpoint `/api/v1/auth/login` para obtener un JWT
                                y luego envía `Authorization: Bearer <token>`. El rol/autoridad se
                                resuelve desde la base de datos, no desde el token.
                                """))
                .addServersItem(new Server().url("http://localhost:8080").description("Servidor local"))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
