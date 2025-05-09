package io.autoinvestor.filters;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AddHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddHeaderGatewayFilterFactory.class);
    private static final String MY_HEADER_KEY = "Alfredo-Header-Key-Request";
    private static final String ANOTHER_HEADER_KEY = "Alfredo-Header-Response";

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerWebExchange updatedExchange = exchange.mutate().request(request -> request.headers(headers -> {
                headers.put(MY_HEADER_KEY, List.of("gen-" + LocalDateTime.now()));

                LOGGER.info("Processed request, added " + MY_HEADER_KEY + " header with vaue: "
                        + List.of("gen-" + LocalDateTime.now()));
            })).build();

            return chain.filter(updatedExchange).then(Mono.fromRunnable(() -> {
                HttpHeaders headers = exchange.getResponse().getHeaders();
                headers.add(ANOTHER_HEADER_KEY, "Iniesta");
                LOGGER.info("Processed response, added " + ANOTHER_HEADER_KEY + " header");
            }));
        };
    }
}
