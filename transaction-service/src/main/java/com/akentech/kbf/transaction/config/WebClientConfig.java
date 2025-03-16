package com.akentech.kbf.transaction.config;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.util.Objects;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient incomeWebClient(WebClient.Builder webClientBuilder, Tracer tracer, Propagator propagator) {
        return webClientBuilder
                .baseUrl("http://localhost:8081")
                .filter(tracingFilter(tracer, propagator))
                .build();
    }

    @Bean
    public WebClient expenseWebClient(WebClient.Builder webClientBuilder, Tracer tracer, Propagator propagator) {
        return webClientBuilder
                .baseUrl("http://localhost:8082")
                .filter(tracingFilter(tracer, propagator))
                .build();
    }

    @Bean
    public WebClient investmentWebClient(WebClient.Builder webClientBuilder, Tracer tracer, Propagator propagator) {
        return webClientBuilder
                .baseUrl("http://localhost:8083")
                .filter(tracingFilter(tracer, propagator))
                .build();
    }

    private ExchangeFilterFunction tracingFilter(Tracer tracer, Propagator propagator) {
        return (clientRequest, next) -> {
            var traceContext = tracer.currentTraceContext().context();
            if (traceContext != null) {
                propagator.inject(Objects.requireNonNull(traceContext), clientRequest.headers(), (carrier, key, value) -> {
                    if (carrier != null) {
                        carrier.add(key, value);
                    }
                });
            }
            return next.exchange(clientRequest);
        };
    }
}
