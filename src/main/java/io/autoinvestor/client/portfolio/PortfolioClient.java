package io.autoinvestor.client.portfolio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class PortfolioClient {

    private final WebClient webClient;

    public PortfolioClient(
            WebClient.Builder webClientBuilder,
            @Value("${autoinvestor.client.portfolio.url}") String baseUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<List<PortfolioHoldingResponse>> getHoldings(String userId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/portfolio/holdings")
                        .build())
                .header("X-User-Id", userId)
                .exchangeToMono(clientResponse -> Mono.defer(() -> {
                    if (clientResponse.statusCode().value() == HttpStatus.OK.value()) {
                        return clientResponse.bodyToMono(PortfolioHoldingResponse[].class).map(Arrays::asList);
                    }
                    return clientResponse.createError();
                }));
    }
}
