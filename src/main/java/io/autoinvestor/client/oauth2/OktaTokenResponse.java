package io.autoinvestor.client.oauth2;

public record OktaTokenResponse(
        String token_type,
        Integer expires_in,
        String access_token,
        String scope,
        String id_token
) {}

