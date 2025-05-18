package io.autoinvestor.controller.auth;

import io.autoinvestor.client.oauth2.OAuth2Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuth2Client oAuth2Client;

    @Value("${autoinvestor.tokenCookieName}")
    private String tokenCookieName;

    @Value("${autoinvestor.okta.redirectUri}")
    private String oktaRedirectUri;

    @Value("${autoinvestor.baseUrl}")
    private String domain;

    @GetMapping("/login")
    public ResponseEntity<?> login() {
        return ResponseEntity
                .status(HttpStatus.SEE_OTHER)
                .location(oAuth2Client.getAuthorizationUri("code", "openid", oktaRedirectUri))
                .build();
    }

    @GetMapping("/callback")
    public Mono<ResponseEntity<?>> callback(@RequestParam("code") String code, @RequestParam("state") UUID state) {
        return oAuth2Client.token("authorization_code", code, oktaRedirectUri)
                .map(accessToken -> ResponseEntity
                        .status(HttpStatus.SEE_OTHER)
                        .location(URI.create("%s/dashboard".formatted(domain)))
                        .header(HttpHeaders.SET_COOKIE, jwtCookie(accessToken).toString())
                        .build());
    }

    private ResponseCookie jwtCookie(String accessToken) {
        return ResponseCookie.from(tokenCookieName, accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/api")
                .sameSite("Strict")
                .build();
    }
}
