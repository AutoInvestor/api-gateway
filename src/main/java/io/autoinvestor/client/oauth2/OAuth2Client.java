package io.autoinvestor.client.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OAuth2Client {

    private final WebClient webClient;

    private final String clientId;
    private final String baseUrl;

    public OAuth2Client(
            WebClient.Builder webClientBuilder,
            @Value("${autoinvestor.client.oauth2.url}") String baseUrl,
            @Value("${autoinvestor.client.oauth2.authorization.basic.username}") String clientId,
            @Value("${autoinvestor.client.oauth2.authorization.basic.password}") String clientSecret
    ) {
        var oauth2Filter = ExchangeFilterFunction.ofRequestProcessor(clientRequest -> Mono.just(
                ClientRequest.from(clientRequest)
                        .headers(headers -> headers.setBasicAuth(clientId, clientSecret, StandardCharsets.UTF_8))
                        .build()
        ));
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .filter(oauth2Filter)
                .build();
        this.clientId = clientId;
        this.baseUrl = baseUrl;
    }

    public URI getAuthorizationUri(String responseType, String scope, String redirectUri) {
        Map<String, String> params = Map.of(
                "client_id", clientId,
                "response_type", responseType,
                "scope", scope,
                "redirect_uri", redirectUri,
                "state", UUID.randomUUID().toString()
        );

        var queryString = params.entrySet().stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)))
                .collect(Collectors.joining("&"));

        return URI.create("%s/authorize?%s".formatted(baseUrl, queryString));
    }

    public Mono<String> token(String grantType, String code, String redirectUri) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/token").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", grantType)
                        .with("code", code)
                        .with("redirect_uri", redirectUri))
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(OktaTokenResponse.class))
                .map(OktaTokenResponse::access_token);
    }
}
