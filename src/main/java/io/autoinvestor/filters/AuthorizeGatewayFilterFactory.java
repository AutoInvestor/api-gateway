package io.autoinvestor.filters;

import io.autoinvestor.configuration.CookieBearerTokenAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizeGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final ReactiveJwtDecoder reactiveJwtDecoder;

    @Value("${autoinvestor.tokenCookieName}")
    private String tokenCookieName;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> new WebFilterChainProxy(securityConfiguration()).filter(exchange, chain::filter);
    }

    private SecurityWebFilterChain securityConfiguration() {
        return ServerHttpSecurity.http()
                .oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec
                                .jwt(jwtSpec -> jwtSpec.jwtDecoder(reactiveJwtDecoder))
                                .bearerTokenConverter(new CookieBearerTokenAuthenticationConverter(tokenCookieName))
                )
                .authorizeExchange(authorizeExchangeSpec ->
                        authorizeExchangeSpec.anyExchange().authenticated()
                )
                .headers(ServerHttpSecurity.HeaderSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .build();
    }
}
