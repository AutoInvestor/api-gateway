package io.autoinvestor.client.users;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        Integer riskLevel
) {
}
