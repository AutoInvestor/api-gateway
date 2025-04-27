package io.autoinvestor.inlineHook;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class InlineController {

    @PostMapping("/api/inlineHook")
    public InLineResponseObject handle (@RequestBody JsonNode body) {

        Map<String,Object> value = new HashMap<>();
        value.put("op", "add");
        value.put("path", "/claims/appId");
        value.put("value", "0123456789");

        Map<String, Object> command = new HashMap<>();
        command.put("type", "com.okta.access.patch");
        command.put("value", List.of(value));

        InLineResponseObject inLineResponseObject = new InLineResponseObject();
        inLineResponseObject.setCommands(List.of(command));

        return inLineResponseObject;
    }

}
