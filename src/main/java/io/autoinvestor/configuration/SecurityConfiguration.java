package io.autoinvestor.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.WebFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final ReactiveJwtDecoder reactiveJwtDecoder;

    @Value("${autoinvestor.okta.hookAuthHeaderName}")
    private String apiAuthHeaderName;

    @Value("${autoinvestor.okta.hookAuthHeaderValue}")
    private String apiAuthHeaderValue;

    @Bean
    @Order(1)
    public SecurityWebFilterChain hookSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/api/hook/**"))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .addFilterAt(hookAuthenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    @Order(2)
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.anyExchange())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers("/api/oauth2/**", "/api/login/**").permitAll()
                        .anyExchange().permitAll()
                )
                .oauth2Login(Customizer.withDefaults())
                .oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec
                        .jwt(jwtSpec -> jwtSpec.jwtDecoder(reactiveJwtDecoder))
                )
                .build();
    }

    private WebFilter hookAuthenticationWebFilter() {
        return (exchange, chain) -> {
            String headerValue = exchange.getRequest().getHeaders().getFirst(apiAuthHeaderName);

            if (apiAuthHeaderValue.equals(headerValue)) {
                return chain.filter(exchange);
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }
}
