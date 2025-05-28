package io.autoinvestor.client.users;

public record UserRequest(
        String email,
        String firstName,
        String lastName
) {
}
