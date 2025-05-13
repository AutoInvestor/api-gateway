package io.autoinvestor.inlineHook;

import io.autoinvestor.client.users.UserResponse;
import io.autoinvestor.client.users.UsersClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class InlineController {

    private final UsersClient usersClient;

    @PostMapping("/inlineHook")
    public Mono<InLineResponseObject> handle(@RequestBody InLineRequestObject body) {
        return Mono.fromSupplier(() -> body.data().access().claims().sub())
                .flatMap(this::getOrCreateUser)
                .map(InlineController::createAddClaimInlineResponseObject);
    }

    private Mono<UUID> getOrCreateUser(String email) {
        return usersClient.getUser(email)
                .flatMap(user -> {
                    log.info("Inline webhook called for existing user with ID: {}", user.userId());
                    return Mono.fromSupplier(user::userId);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Inline webhook called for non-existing user with email: {}", email);
                    return usersClient.createUser(email)
                            .then(Mono.defer(() -> usersClient.getUser(email)))
                            .map(UserResponse::userId);
                }));
    }

    private static InLineResponseObject createAddClaimInlineResponseObject(UUID userId) {
        var accessPatch = new InLineResponseObject.Command("com.okta.access.patch", List.of(addUserIdClaim(userId)));
        var identityPatch = new InLineResponseObject.Command("com.okta.identity.patch", List.of(addUserIdClaim(userId)));
        return new InLineResponseObject(List.of(accessPatch, identityPatch));
    }

    private static InLineResponseObject.Command.Value addUserIdClaim(UUID userId) {
        return new InLineResponseObject.Command.Value(
                "add",
                "/claims/userId",
                userId.toString()
        );
    }
}
