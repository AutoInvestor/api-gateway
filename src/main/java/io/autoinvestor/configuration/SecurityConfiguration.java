package io.autoinvestor.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        var authenticationEntryPoint = new RedirectServerAuthenticationEntryPoint("/api/oauth2/authorization/okta");
        var authenticationSuccessHandler = new RedirectServerAuthenticationSuccessHandler("/dashboard");

        return http
                .securityMatcher(ServerWebExchangeMatchers.anyExchange())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers("/", "/api/oauth2/**", "/api/login/**").permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(exceptionHandlingSpec ->
                        exceptionHandlingSpec.authenticationEntryPoint(authenticationEntryPoint)
                )
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(authenticationSuccessHandler)
                )
                .build();
    }
}
