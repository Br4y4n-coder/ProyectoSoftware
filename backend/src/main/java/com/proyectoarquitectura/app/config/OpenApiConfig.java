package com.proyectoarquitectura.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SIT - Sistema de Tickets de Soporte Interno")
                        .description("""
                                API REST para la gestión de tickets de soporte interno.

                                **Roles disponibles:**
                                - `ADMINISTRADOR`: acceso total al sistema
                                - `AGENTE`: gestión y asignación de tickets
                                - `CLIENTE`: creación y seguimiento de sus propios tickets

                                **Autenticación:** Bearer JWT — obtén el token desde `POST /api/auth/login` y úsalo en el botón **Authorize**.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("soporte@proyectoarquitectura.com")))
                .servers(List.of(
                        new Server().url("https://proyecto-ticket-26xq.onrender.com").description("Producción"),
                        new Server().url("http://localhost:8080").description("Servidor local")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtenido del endpoint POST /api/auth/login")));
    }
}
