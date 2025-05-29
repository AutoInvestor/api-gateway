package io.autoinvestor.client.marketfeeling;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class MarketFeelingClient {

    private final WebClient webClient;

    public MarketFeelingClient(
            WebClient.Builder webClientBuilder,
            @Value("${autoinvestor.client.market-feeling.url}") String baseUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<List<NewsResponse>> getNews(String userId, String assetId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/news")
                        .queryParam("assetId", assetId)
                        .build())
                .header("X-User-Id", userId)
                .exchangeToMono(clientResponse -> Mono.defer(() -> {
                    if (clientResponse.statusCode().value() == HttpStatus.OK.value()) {
                        return clientResponse.bodyToMono(NewsResponse[].class).map(Arrays::asList);
                    }
                    return clientResponse.createError();
                }));
    }
}
