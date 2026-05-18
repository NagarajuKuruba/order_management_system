package com.spry.oms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        log.info("Configuring Swagger/OpenAPI documentation");

        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("Order Management System API")
                        .version("1.0")
                        .description("Async Order Processing APIs")
                        .contact(new Contact()
                                .name("OMS Support Team")
                                .email("support@test.com"))
                        .license(new License()
                                .name("Apache 2.0")));

        log.debug("OpenAPI configuration completed - Title: Order Management System API, Version: 1.0");
        return openAPI;
    }
}
