package io.autoinvestor.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizeHookGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> new WebFilterChainProxy(securityConfiguration()).filter(exchange, chain::filter);
    }

    public SecurityWebFilterChain securityConfiguration() {
        return ServerHttpSecurity.http()
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )
                .addFilterAt(headerAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    public WebFilter headerAuthenticationFilter() {
        return (exchange, chain) -> {
            String headerValue = exchange.getRequest().getHeaders().getFirst("X-Hook-Authentication");

            if ("tremebundo-hook".equals(headerValue)) {
                return chain.filter(exchange);
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }
}
