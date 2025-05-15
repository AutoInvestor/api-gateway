package io.autoinvestor.controller.inlinehook;

import java.util.List;

public record TokenHookResponse(
        List<Command> commands
) {
    public record Command(
            String type,
            List<Value> value
    ) {
        public record Value(
                String op,
                String path,
                String value
        ) {
        }
    }
}
