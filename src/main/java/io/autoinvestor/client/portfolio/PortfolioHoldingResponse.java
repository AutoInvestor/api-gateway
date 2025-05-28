package io.autoinvestor.client.portfolio;

public record PortfolioHoldingResponse(
        String assetId,
        Integer amount,
        Integer price
) {
}
