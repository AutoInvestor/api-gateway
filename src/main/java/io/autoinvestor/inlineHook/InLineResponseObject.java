package io.autoinvestor.inlineHook;

import java.util.List;

public record InLineResponseObject(
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
