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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimToHeaderGatewayFilterFactory implements GatewayFilterFactory<ClaimToHeaderGatewayFilterFactory.Config> {

    private final ServerWebExchangeFactory serverWebExchangeFactory;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication instanceof JwtAuthenticationToken)
                .map(jtwAuthenticationToken -> addClaimToHeader(exchange, (JwtAuthenticationToken) jtwAuthenticationToken, config))
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    private ServerWebExchange addClaimToHeader(ServerWebExchange exchange, JwtAuthenticationToken jwtAuthenticationToken, Config config) {
        return Optional
                .ofNullable(jwtAuthenticationToken.getTokenAttributes().get(config.getClaim()))
                .map(claim -> switch (claim) {
                    case String value -> serverWebExchangeFactory.withHeader(exchange, config.getHeaderName(), value);
                    case List<?> list when list.stream().allMatch(v -> v instanceof String) -> {
                        var values = list.stream().map(v -> (String) v).toList();
                        yield serverWebExchangeFactory.withHeader(exchange, config.getHeaderName(), values);
                    }
                    default -> {
                        log.warn("Received value is not a String or List<String>");
                        yield exchange;
                    }
                })
                .orElseGet(() -> {
                    log.warn("Claim {} not found", config.getClaim());
                    return exchange;
                });
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
