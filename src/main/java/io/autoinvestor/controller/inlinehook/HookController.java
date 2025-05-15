package io.autoinvestor.controller.inlinehook;

import io.autoinvestor.client.users.UserResponse;
import io.autoinvestor.client.users.UsersClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/hook")
@RequiredArgsConstructor
public class HookController {

    private final UsersClient usersClient;

    @PostMapping("/register")
    public Mono<Void> register(@RequestBody RegisterHookRequest body) {
        return usersClient.createUser(body.data().userProfile().email());
    }

    @PostMapping("/token")
    public Mono<TokenHookResponse> token(@RequestBody TokenHookRequest body) {
        return usersClient.getUser(body.data().access().claims().sub())
                .map(UserResponse::userId)
                .map(HookController::createAddClaimInlineResponseObject);
    }

    private static TokenHookResponse createAddClaimInlineResponseObject(UUID userId) {
        var accessPatch = new TokenHookResponse.Command("com.okta.access.patch", List.of(addUserIdClaim(userId)));
        var identityPatch = new TokenHookResponse.Command("com.okta.identity.patch", List.of(addUserIdClaim(userId)));
        return new TokenHookResponse(List.of(accessPatch, identityPatch));
    }

    private static TokenHookResponse.Command.Value addUserIdClaim(UUID userId) {
        return new TokenHookResponse.Command.Value(
                "add",
                "/claims/userId",
                userId.toString()
        );
    }
}
