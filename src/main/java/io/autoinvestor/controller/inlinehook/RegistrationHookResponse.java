package io.autoinvestor.controller.inlinehook;

import java.util.List;

public record RegistrationHookResponse(
    List<Command> commands
) {
    public record Command(
            String type,
            Value value
    ) {
        public record Value(
                String registration
        ) {
        }
    }
}
