package io.autoinvestor.controller.inlinehook;

public record RegisterHookRequest(
    Data data
) {
    public record Data(
        UserProfile userProfile
    ) {
        public record UserProfile(
                String firstName,
                String lastName,
                String login,
                String email
        ) {
        }
    }
}
