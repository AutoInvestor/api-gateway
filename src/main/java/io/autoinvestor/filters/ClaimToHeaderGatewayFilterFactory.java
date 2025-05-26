package io.autoinvestor.filters;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimToHeaderGatewayFilterFactory implements GatewayFilterFactory<ClaimToHeaderGatewayFilterFactory.Config> {

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication instanceof OAuth2AuthenticationToken)
                .map(authentication -> (OAuth2AuthenticationToken) authentication)
                .mapNotNull(authentication -> authentication.getPrincipal().getAttribute(config.getClaim()))
                .filter(userId -> userId instanceof String)
                .map(userId -> withHeader(exchange, config.getHeaderName(), (String) userId))
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    private static ServerWebExchange withHeader(ServerWebExchange exchange, String headerName, String headerValue) {
        return exchange.mutate().request(request -> request.headers(headers ->
                headers.add(headerName, headerValue)
        )).build();
    }

    @Override
    public Config newConfig() {
        return new Config();
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("claim", "headerName");
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static class Config {
        private String claim;
        private String headerName;
    }
}
