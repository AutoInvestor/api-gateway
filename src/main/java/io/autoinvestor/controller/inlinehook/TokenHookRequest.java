package io.autoinvestor.controller.inlinehook;

public record TokenHookRequest(
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
