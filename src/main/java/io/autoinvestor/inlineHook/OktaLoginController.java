package io.autoinvestor.inlineHook;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
public class OktaLoginController {

    private final String oktaClientId;
    private final String oktaResourceUri;
    private final String oktaRedirectUri;
    private final String oktaClientSecret;

    private final WebClient webClient;

    public OktaLoginController(
            @Value("${autoinvestor.okta.clientId}") String oktaClientId,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String oktaResourceUri,
            @Value("${autoinvestor.okta.redirectUri}") String oktaRedirectUri,
            @Value("${autoinvestor.okta.clientSecret}") String oktaClientSecret,
            WebClient.Builder webClientBuilder) {
        this.oktaClientId = oktaClientId;
        this.oktaResourceUri = oktaResourceUri;
        this.oktaRedirectUri = oktaRedirectUri;
        this.oktaClientSecret = oktaClientSecret;
        this.webClient = webClientBuilder.baseUrl(oktaResourceUri).build();
    }

    @GetMapping("/login")
    public ResponseEntity<?> login() {
        Map<String, String> params = Map.of(
                "client_id", oktaClientId,
                "response_type", "code",
                "scope", "openid",
                "redirect_uri", oktaRedirectUri,
                "state", UUID.randomUUID().toString()
        );

        var queryString = params.entrySet().stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)))
                .collect(Collectors.joining("&"));

        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create("%s/v1/authorize?%s".formatted(oktaResourceUri, queryString))).build();
    }

    @GetMapping("/callback")
    public Mono<ResponseEntity<?>> callback(@RequestParam("code") String code, @RequestParam("state") UUID state) {
        return webClient.post()
                .uri("/v1/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("code", code)
                        .with("redirect_uri", oktaRedirectUri))
                .header(HttpHeaders.AUTHORIZATION, basicAuthHeader(oktaClientId, oktaClientSecret))
                .retrieve()
                .bodyToMono(OktaTokenResponse.class)
                .map(response -> ResponseEntity.ok().body(response.access_token()));
    }

    private static String basicAuthHeader(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString("%s:%s".formatted(username, password).getBytes(StandardCharsets.UTF_8));
    }
}
