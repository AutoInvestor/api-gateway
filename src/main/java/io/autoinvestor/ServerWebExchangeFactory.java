package io.autoinvestor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@Slf4j
@Component
public class ServerWebExchangeFactory {
    public ServerWebExchange withoutAuthorization(ServerWebExchange exchange) {
        return exchange.mutate().request(request -> request.headers(headers ->
                headers.remove(HttpHeaders.AUTHORIZATION)
        )).build();
    }

    public ServerWebExchange withHeader(ServerWebExchange exchange, String headerName, String headerValue) {
        return exchange.mutate().request(request -> request.headers(headers ->
                headers.add(headerName, headerValue)
        )).build();
    }

    public ServerWebExchange withHeader(ServerWebExchange exchange, String headerName, List<String> headerValue) {
        return exchange.mutate().request(request -> request.headers(headers ->
                headers.addAll(headerName, headerValue)
        )).build();
    }
}
