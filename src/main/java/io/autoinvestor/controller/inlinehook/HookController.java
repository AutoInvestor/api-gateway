package io.autoinvestor.controller.inlinehook;

import io.autoinvestor.client.users.UsersClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/hook")
@RequiredArgsConstructor
public class HookController {

    private final UsersClient usersClient;

    @PostMapping("/register")
    public Mono<ResponseEntity<?>> register(@RequestBody RegisterHookRequest body) {
        return usersClient.createUser(body.data().userProfile().email())
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
