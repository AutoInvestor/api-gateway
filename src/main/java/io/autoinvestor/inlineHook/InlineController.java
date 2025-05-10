package io.autoinvestor.inlineHook;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class InlineController {

    private final RestTemplate restTemplate;

    @PostMapping("/api/inlineHook")
    public InLineResponseObject handle(@RequestBody InLineRequestObject body) {
        String email = body.data().access().claims().sub();
        String userId = fetchUserId(email);

        var accessPatch = new InLineResponseObject.Command("com.okta.access.patch", List.of(addUserIdClaim(userId)));
        var identityPatch = new InLineResponseObject.Command("com.okta.identity.patch", List.of(addUserIdClaim(userId)));

        return new InLineResponseObject(List.of(accessPatch, identityPatch));
    }

    private static InLineResponseObject.Command.Value addUserIdClaim(String userId) {
        return new InLineResponseObject.Command.Value(
                "add",
                "/claims/userId",
                userId
        );
    }

    private String fetchUserId(String email) {
        String url = "http://users-service/api/userId";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return response.getBody();
    }
}
