package io.autoinvestor.inlineHook;

import io.autoinvestor.client.users.UsersClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InlineController {

    private final UsersClient usersClient;

    @PostMapping("/api/inlineHook")
    public Mono<InLineResponseObject> handle(@RequestBody InLineRequestObject body) {
        String email = body.data().access().claims().sub();

        return usersClient.getUser(email)
                .switchIfEmpty(usersClient.createUser(email))
                .map(userResponse -> createAddClaimInlineResponseObject(userResponse.userId()));
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
