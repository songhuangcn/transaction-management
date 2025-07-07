package com.example.transaction.management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development Environment Server");

        Server prodServer = new Server();
        prodServer.setUrl("https://transaction-management.hdgcs.com");
        prodServer.setDescription("Production Environment Server");

        Contact contact = new Contact();
        contact.setEmail("contact@example.com");
        contact.setName("Transaction Management System");
        contact.setUrl("https://www.example.com");

        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Transaction Management System API")
                .version("1.0")
                .contact(contact)
                .description("Comprehensive REST API for managing financial transactions with full CRUD operations, validation, and pagination support")
                .termsOfService("https://www.example.com/terms")
                .license(mitLicense);

        return new OpenAPI().info(info).servers(List.of(devServer, prodServer));
    }
}