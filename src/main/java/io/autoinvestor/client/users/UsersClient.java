package io.autoinvestor.client.users;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UsersClient {

    private final WebClient webClient;

    public UsersClient(
            WebClient.Builder webClientBuilder,
            @Value("${autoinvestor.client.users.url}") String baseUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<UserResponse> getUser(String email) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/user")
                        .queryParam("email", email).build())
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        return clientResponse.bodyToMono(UserResponse.class);
                    }
                    return Mono.empty();
                });
    }

    public Mono<UserResponse> createUser(String email) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/user").build())
                .body(new UserRequest(email), UserRequest.class)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        return clientResponse.bodyToMono(UserResponse.class);
                    }
                    return Mono.empty();
                });
    }
}
