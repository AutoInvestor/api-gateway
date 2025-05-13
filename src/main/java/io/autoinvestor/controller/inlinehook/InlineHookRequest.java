package io.autoinvestor.controller.inlinehook;

public record InlineHookRequest(
        Data data
) {
    public record Data(
            Access access
    ) {
        public record Access(
                Claims claims
        ) {
            public record Claims(
                    String sub
            ) {
            }
        }
    }
}
