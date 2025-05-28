package com.example.trackexpenses.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Track Expenses API",
                description = "API do śledzenia wydatków osobistych",
                version = "1.0.0"
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Development Server")
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "Wpisz JWT token (bez 'Bearer ' na początku)"
)
public class OpenApiConfig {
}