package io.autoinvestor.filters;

import io.autoinvestor.ServerWebExchangeFactory;
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

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimToHeaderGatewayFilterFactory implements GatewayFilterFactory<ClaimToHeaderGatewayFilterFactory.Config> {

    private final ServerWebExchangeFactory serverWebExchangeFactory;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication instanceof OAuth2AuthenticationToken)
                .map(authentication -> (OAuth2AuthenticationToken) authentication)
                .mapNotNull(authentication -> authentication.getPrincipal().getAttribute(config.getClaim()))
                .map(userId -> serverWebExchangeFactory.withHeader(exchange, config.getHeaderName(), userId.toString()))
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
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
