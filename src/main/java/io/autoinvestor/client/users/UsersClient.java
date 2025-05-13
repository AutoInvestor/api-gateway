package io.autoinvestor.client.users;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
                        .path("/user")
                        .queryParam("email", email).build())
                .exchangeToMono(clientResponse -> Mono.defer(() -> {
                    if (clientResponse.statusCode().value() == HttpStatus.OK.value()) {
                        return clientResponse.bodyToMono(UserResponse.class);
                    } else if (clientResponse.statusCode().value() == HttpStatus.NOT_FOUND.value()) {
                        return Mono.empty();
                    }
                    return clientResponse.createError();
                }));
    }

    public Mono<Void> createUser(String email) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/user").build())
                .bodyValue(new UserRequest(email))
                .exchangeToMono(clientResponse -> Mono.defer(() -> {
                    if (clientResponse.statusCode().value() == HttpStatus.CREATED.value()) {
                        return Mono.empty();
                    }
                    return clientResponse.createError();
                }));
    }
}
