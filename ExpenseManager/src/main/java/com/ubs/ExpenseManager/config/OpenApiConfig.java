package com.ubs.ExpenseManager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port}")
    private String serverPort;

    @Bean
    public OpenAPI expenseManagerOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:" + serverPort);
        localServer.setDescription("Local Server");

        Server devServer = new Server();
        devServer.setUrl("www.dev.ubs.api");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("www.prod.ubs.api");
        prodServer.setDescription("Production Server");

        Contact contact = new Contact();
        contact.setName("UBS Expense Manager Team");

        License license = new License()
                .name("© UBS 1998 - 2025. All rights reserved.");

        Info info = new Info()
                .title("UBS Expense Manager API")
                .version("1.0.0")
                .description("Employee Expense Management System API Documentation")
                .contact(contact)
                .license(license);

        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, devServer, prodServer))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .schemaRequirement("bearerAuth", bearerAuth);

    }
}
