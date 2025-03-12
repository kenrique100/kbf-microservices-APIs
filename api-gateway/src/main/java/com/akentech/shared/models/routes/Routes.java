package com.akentech.shared.models.routes;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@Configuration
public class Routes {

    private static final Logger logger = LoggerFactory.getLogger(Routes.class);

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        logger.info("Configuring routes with circuit breakers...");
        return builder.routes()
                .route("income-service", r -> r.path("/api/incomes/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("incomeServiceCircuitBreaker")
                                .setFallbackUri("forward:/fallback")))
                        .uri("http://localhost:8081"))
                .route("income-service-swagger", r -> r.path("/aggregate/income-service/v3/api-docs")
                        .filters(f -> {
                            logger.info("Configuring circuit breaker for income-service-swagger");
                            return f.rewritePath("/aggregate/income-service/v3/api-docs", "/v3/api-docs")
                                    .circuitBreaker(config -> config
                                            .setName("incomeServiceCircuitBreaker")
                                            .setFallbackUri("forward:/fallback"));
                        })
                        .uri("http://localhost:8081"))
                .route("expense-service", r -> r.path("/api/expenses/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("expenseServiceCircuitBreaker")
                                .setFallbackUri("forward:/fallback")))
                        .uri("http://localhost:8082"))
                .route("expense-service-swagger", r -> r.path("/aggregate/expense-service/v3/api-docs")
                        .filters(f -> {
                            logger.info("Configuring circuit breaker for expense-service-swagger");
                            return f.rewritePath("/aggregate/expense-service/v3/api-docs", "/v3/api-docs")
                                    .circuitBreaker(config -> config
                                            .setName("expenseServiceCircuitBreaker")
                                            .setFallbackUri("forward:/fallback"));
                        })
                        .uri("http://localhost:8082"))
                .route("investments-service", r -> r.path("/api/investments/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("investmentServiceCircuitBreaker")
                                .setFallbackUri("forward:/fallback")))
                        .uri("http://localhost:8083"))
                .route("investments-service-swagger", r -> r.path("/aggregate/investments-service/v3/api-docs")
                        .filters(f -> {
                            logger.info("Configuring circuit breaker for investments-service-swagger");
                            return f.rewritePath("/aggregate/investments-service/v3/api-docs", "/v3/api-docs")
                                    .circuitBreaker(config -> config
                                            .setName("investmentServiceCircuitBreaker")
                                            .setFallbackUri("forward:/fallback"));
                        })
                        .uri("http://localhost:8083"))
                .route("transaction-service", r -> r.path("/api/transactions/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("transactionServiceCircuitBreaker")
                                .setFallbackUri("forward:/fallback")))
                        .uri("http://localhost:8084"))
                .route("transaction-service-swagger", r -> r.path("/aggregate/transaction-service/v3/api-docs")
                        .filters(f -> {
                            logger.info("Configuring circuit breaker for transaction-service-swagger");
                            return f.rewritePath("/aggregate/transaction-service/v3/api-docs", "/v3/api-docs")
                                    .circuitBreaker(config -> config
                                            .setName("transactionServiceCircuitBreaker")
                                            .setFallbackUri("forward:/fallback"));
                        })
                        .uri("http://localhost:8084"))
                .route("actuator-health", r -> r.path("/api/actuator/health")
                        .filters(f -> f.rewritePath("/api/actuator/health", "/actuator/health"))
                        .uri("http://localhost:8085"))
                .build();
    }

    @Bean
    public RouteLocator fallbackRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("fallbackRoute", r -> r.path("/fallback")
                        .filters(f -> f.setStatus(HttpStatus.SERVICE_UNAVAILABLE)
                                .modifyResponseBody(String.class, String.class,
                                        (exchange, s) -> Mono.just("{\"message\": \"Service is currently unavailable. Please try again later.\"}")
                                )
                        )
                        .uri("no://op") // Placeholder URI
                )
                .build();
    }
}