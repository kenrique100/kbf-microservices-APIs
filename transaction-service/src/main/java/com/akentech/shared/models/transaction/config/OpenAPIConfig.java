package com.akentech.shared.models.transaction.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI transactionServiceAPI() {
        return new OpenAPI()
                .info(new Info().title("Transaction Service API")
                        .description("This is the REST APT for Transaction Service Belonging to KBF Microservice web app")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.8")))
                .externalDocs(new ExternalDocumentation()
                        .description("You can refer to the Transaction Service Jira Documentation")
                        .url("https://Transaction-service-dummy-url.com/docs"));
    }
}