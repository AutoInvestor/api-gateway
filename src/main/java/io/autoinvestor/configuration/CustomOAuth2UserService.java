package io.autoinvestor.configuration;

import io.autoinvestor.client.users.UsersClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService implements ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UsersClient usersClient;

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) {
        ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultReactiveOAuth2UserService();

        return delegate.loadUser(userRequest)
                .flatMap(oauth2User -> fetchUserId(oauth2User)
                        .map(userId -> {
                            Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
                            attributes.put("userId", userId);
                            return new DefaultOAuth2User(
                                    oauth2User.getAuthorities(),
                                    attributes,
                                    "sub"
                            );
                        })
                        .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                );
    }

    private Mono<String> fetchUserId(OAuth2User user) {
        return usersClient
                .getUser(user.getAttribute("email"))
                .map(userResponse -> userResponse.userId().toString());
    }
}

