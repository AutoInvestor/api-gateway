package io.autoinvestor.client.marketfeeling;

import java.time.LocalDateTime;

public record NewsResponse(
        String title,
        LocalDateTime date,
        String url,
        String assetId
) {
}
