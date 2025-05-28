package io.autoinvestor.controller;

import io.autoinvestor.client.marketfeeling.MarketFeelingClient;
import io.autoinvestor.client.marketfeeling.NewsResponse;
import io.autoinvestor.client.portfolio.PortfolioClient;
import io.autoinvestor.client.portfolio.PortfolioHoldingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/internal/news")
@RequiredArgsConstructor
public class NewsController {

    private final PortfolioClient portfolioClient;
    private final MarketFeelingClient marketFeelingClient;

    @GetMapping
    public Mono<ResponseEntity<?>> getNews(@RequestHeader("X-User-Id") String userId) {
        return portfolioClient.getHoldings(userId)
                .map(holdings -> holdings.stream().map(PortfolioHoldingResponse::assetId).toList())
                .map(assetIds -> assetIds.stream().map(assetId -> marketFeelingClient.getNews(userId, assetId)).toList())
                .flatMap(NewsController::flatten)
                .map(list -> list.stream().sorted(Comparator.comparing(NewsResponse::date).reversed()).toList())
                .map(ResponseEntity::ok);
    }

    private static <T> Mono<List<T>> flatten(List<Mono<List<T>>> monoList) {
        return Flux.fromIterable(monoList)
                .flatMap(mono -> mono)
                .flatMapIterable(list -> list)
                .collectList();
    }
}
