package io.autoinvestor.inlineHook;

public record OktaTokenResponse(
        String token_type,
        int expires_in,
        String access_token,
        String scope,
        String id_token
) {}

