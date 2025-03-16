package com.akentech.kbf.gateway.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import io.micrometer.tracing.brave.bridge.BravePropagator;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import brave.Tracing;
import brave.propagation.StrictCurrentTraceContext;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

@Configuration
public class ObservationConfig {

    private final ConcurrentKafkaListenerContainerFactory<?, ?> concurrentKafkaListenerContainerFactory;

    public ObservationConfig(ConcurrentKafkaListenerContainerFactory<?, ?> concurrentKafkaListenerContainerFactory) {
        this.concurrentKafkaListenerContainerFactory = concurrentKafkaListenerContainerFactory;
    }

    @PostConstruct
    public void setObservationForKafkaTemplate() {
        concurrentKafkaListenerContainerFactory.getContainerProperties().setObservationEnabled(true);
    }

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }

    @Bean
    public Tracing tracing() {
        return Tracing.newBuilder()
                .currentTraceContext(StrictCurrentTraceContext.create())
                .build();
    }

    @Bean
    public Tracer tracer(Tracing tracing) {
        return new BraveTracer(tracing.tracer(), new BraveCurrentTraceContext(tracing.currentTraceContext()));
    }

    @Bean
    public Propagator propagator(Tracing tracing) {
        return new BravePropagator(tracing);
    }
}