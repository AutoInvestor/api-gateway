package io.autoinvestor.inlineHook;

public record InLineRequestObject(
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
