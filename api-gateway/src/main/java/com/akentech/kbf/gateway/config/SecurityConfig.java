package com.akentech.kbf.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    // List of free resource URLs that do not require authentication
    private final String[] freeResourceUrls = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            "/swagger-resources/**", "/api-docs/**", "/aggregate/**",
            "/webjars/**", "/favicon.ico", "/actuator/health", "/actuator/prometheus"
    };

    /**
     * Configures CORS (Cross-Origin Resource Sharing) for the application.
     *
     * @return A CorsConfigurationSource object with CORS settings.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://localhost:8085"); // Allow frontend origin
        corsConfig.addAllowedMethod("*"); // Allow all HTTP methods (GET, POST, PUT, DELETE, etc.)
        corsConfig.addAllowedHeader("*"); // Allow all headers
        corsConfig.setAllowCredentials(true); // Allow credentials (e.g., cookies)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // Apply CORS to all paths

        System.out.println("CORS configuration loaded in API Gateway!"); // Log statement
        return source;
    }

    /**
     * Configures security for the application.
     *
     * @param http The ServerHttpSecurity object to configure.
     * @return A SecurityWebFilterChain object with security settings.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(freeResourceUrls).permitAll() // Allow access to free resources
                        .anyExchange().authenticated() // Secure all other endpoints
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                        .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF for simplicity
                        .build();
    }

    /**
     * Converts a JWT into an AbstractAuthenticationToken.
     *
     * @return A Converter object for JWT authentication.
     */
    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract roles from the JWT claims
            Collection<String> authorities = jwt.getClaimAsStringList("roles");
            if (authorities == null) {
                authorities = jwt.getClaimAsStringList("realm_access.roles");
            }
            return Flux.fromIterable(authorities == null ? List.of() :
                    authorities.stream()
                            .map(role -> (GrantedAuthority) () -> "ROLE_" + role)
                            .collect(Collectors.toList()));
        });
        return converter;
    }
}