package com.uees.studentservices.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI studentServicesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Student Services API - Grupo B")
                        .description("Plataforma de Cursos en Linea (LMS) - Modulo del Estudiante. " +
                                "Registro JWT, dashboard, progreso, foro WebSocket, certificados por email, " +
                                "consumer RabbitMQ y sincronizacion con EspoCRM.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Universidad Evangelica de El Salvador - UEES")
                                .email("s.davidmelgar@gmail.com"))
                        .license(new License().name("Academic").url("https://uees.edu.sv")))
                .components(new Components()
                        .addSecuritySchemes(SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtenido en POST /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME));
    }
}
