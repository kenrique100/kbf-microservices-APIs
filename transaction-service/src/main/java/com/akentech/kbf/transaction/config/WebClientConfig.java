package com.akentech.kbf.transaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient incomeWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    @Bean
    public WebClient expenseWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    @Bean
    public WebClient investmentWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl("http://localhost:8083").build();
    }
}