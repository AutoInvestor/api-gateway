package io.autoinvestor.inlineHook;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class InlineController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/api/inlineHook")
    public InLineResponseObject handle(@RequestBody JsonNode body) {

        String email = body.path("data").path("access").path("claims").path("sub").asText();
        String userId = fetchUserId(email);

        Map<String, Object> value = new HashMap<>();
        value.put("op", "add");
        value.put("path", "/claims/userId");

        value.put("value", userId);

        Map<String, Object> accessTokenCommand = new HashMap<>();
        accessTokenCommand.put("type", "com.okta.access.patch");
        accessTokenCommand.put("value", List.of(value));

        Map<String, Object> idTokenCommand = new HashMap<>();
        idTokenCommand.put("type", "com.okta.identity.patch");
        idTokenCommand.put("value", List.of(value));

        InLineResponseObject inLineResponseObject = new InLineResponseObject();
        inLineResponseObject.setCommands(List.of(accessTokenCommand, idTokenCommand));

        return inLineResponseObject;
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
