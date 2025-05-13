package io.autoinvestor.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CookieBearerTokenAuthenticationConverter implements ServerAuthenticationConverter {

    private final String cookieName;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(cookieName))
                .map(cookie -> new BearerTokenAuthenticationToken(cookie.getValue()));
    }
}
