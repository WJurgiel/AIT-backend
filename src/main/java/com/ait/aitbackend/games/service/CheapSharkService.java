package com.ait.aitbackend.games.service;

import com.ait.aitbackend.games.cache.CheapSharkDealsCacheService;
import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDetailsDto;
import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.ait.aitbackend.games.dto.cheapshark.CheapSharkGameDetailsDto;
import com.ait.aitbackend.games.dto.cheapshark.CheapSharkGameSearchDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CheapSharkService {
    private final RestClient cheapSharkRestClient;
    private final String normalizedBaseUrl;
    private final String normalizedRedirectBaseUrl;
    private final CheapSharkDealsCacheService dealsCacheService;

    public CheapSharkService(
            RestClient.Builder restClientBuilder,
            @Value("${cheapshark.api.base-url:https://www.cheapshark.com/api/1.0}") String cheapSharkBaseUrl,
            @Value("${cheapshark.redirect.base-url:https://www.cheapshark.com}") String cheapSharkRedirectBaseUrl,
            CheapSharkDealsCacheService dealsCacheService
    ) {
        this.normalizedBaseUrl = cheapSharkBaseUrl.endsWith("/")
                ? cheapSharkBaseUrl.substring(0, cheapSharkBaseUrl.length() - 1)
                : cheapSharkBaseUrl;
        this.normalizedRedirectBaseUrl = cheapSharkRedirectBaseUrl.endsWith("/")
                ? cheapSharkRedirectBaseUrl.substring(0, cheapSharkRedirectBaseUrl.length() - 1)
                : cheapSharkRedirectBaseUrl;
        this.dealsCacheService = dealsCacheService;
        this.cheapSharkRestClient = restClientBuilder.baseUrl(this.normalizedBaseUrl).build();
    }

    public List<CheapSharkDealDto> getDeals(Integer storeId, Integer onSale) {
        return dealsCacheService.getFreshDeals(storeId, onSale)
                .orElseGet(() -> fetchAndCacheDeals(storeId, onSale));
    }

    private List<CheapSharkDealDto> fetchAndCacheDeals(Integer storeId, Integer onSale) {
        List<CheapSharkDealDto> deals = cheapSharkRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/deals")
                        .queryParam("storeID", storeId)
                        .queryParam("onSale", onSale)
                        .build())
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {
                });

        dealsCacheService.saveDeals(storeId, onSale, deals);
        return deals;
    }

    public List<CheapSharkGameSearchDto> searchGamesByTitle(String title) {
        return cheapSharkRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam("title", title)
                        .build())
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {
                });
    }

    public CheapSharkDealDetailsDto getDealById(String dealId) {
        String normalizedDealId = normalizePossiblyEncodedValue(dealId);
        String encodedDealId = URLEncoder.encode(normalizedDealId, StandardCharsets.UTF_8);

        return cheapSharkRestClient.get()
                .uri(URI.create(normalizedBaseUrl + "/deals?id=" + encodedDealId))
                .retrieve()
                .body(CheapSharkDealDetailsDto.class);
    }

    public CheapSharkGameDetailsDto getGameById(String gameId) {
        String normalizedGameId = normalizePossiblyEncodedValue(gameId);
        String encodedGameId = URLEncoder.encode(normalizedGameId, StandardCharsets.UTF_8);

        return cheapSharkRestClient.get()
                .uri(URI.create(normalizedBaseUrl + "/games?id=" + encodedGameId))
                .retrieve()
                .body(CheapSharkGameDetailsDto.class);
    }

    public String buildRedirectUrl(String dealId) {
        String normalizedDealId = normalizePossiblyEncodedValue(dealId);
        String encodedDealId = URLEncoder.encode(normalizedDealId, StandardCharsets.UTF_8);
        return normalizedRedirectBaseUrl + "/redirect?dealID=" + encodedDealId;
    }

    private String normalizePossiblyEncodedValue(String value) {
        if (value == null || !value.contains("%")) {
            return value;
        }

        try {
            // Allow both raw ids and already percent-encoded ids from clients.
            return UriUtils.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return value;
        }
    }
}




