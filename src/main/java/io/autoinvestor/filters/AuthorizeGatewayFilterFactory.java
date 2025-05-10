package io.autoinvestor.filters;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorizeGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final ReactiveJwtDecoder reactiveJwtDecoder;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> new WebFilterChainProxy(securityConfiguration()).filter(exchange, chain::filter);
    }

    private SecurityWebFilterChain securityConfiguration() {
        return ServerHttpSecurity.http()
                .oauth2ResourceServer(oAuth2ResourceServerSpec ->
                        oAuth2ResourceServerSpec.jwt(jwtSpec ->
                                jwtSpec.jwtDecoder(reactiveJwtDecoder)
                        )
                )
                .authorizeExchange(authorizeExchangeSpec ->
                        authorizeExchangeSpec.anyExchange().authenticated()
                )
                .headers(ServerHttpSecurity.HeaderSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // enable if we are calling from frontend
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .build();
    }
}
