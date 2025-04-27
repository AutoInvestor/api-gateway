package io.autoinvestor.inlineHook;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InlineController {

    @PostMapping("/api/inlineHook")
    public InLineResponseObject handle(@RequestBody JsonNode body) {

        Map<String, Object> value = new HashMap<>();
        value.put("op", "add");
        value.put("path", "/claims/userId");
        value.put("value", "0123456789");

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
}
