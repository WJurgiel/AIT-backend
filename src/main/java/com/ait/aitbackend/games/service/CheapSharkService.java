package com.ait.aitbackend.games.service;

import com.ait.aitbackend.games.cache.CheapSharkDealsCacheService;
import com.ait.aitbackend.games.cache.CheapSharkGameCacheDocument;
import com.ait.aitbackend.games.cache.CheapSharkGameCacheRepository;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Serwis integrujący się z API CheapShark.
 * Odpowiada za pobieranie danych o grach, ofertach,
 * cache oraz generowanie linków przekierowań.
 */
@Service
public class CheapSharkService {

    // Domyślne sklepy, jeśli nie podano konkretnego
    private static final List<Integer> DEFAULT_STORE_IDS =
            List.of(1, 7, 25);

    private final RestClient cheapSharkRestClient;

    private final String normalizedBaseUrl;
    private final String normalizedRedirectBaseUrl;

    private final CheapSharkDealsCacheService dealsCacheService;
    private final CheapSharkGameCacheRepository gameCacheRepository;

    private final long gamesCacheTtlSeconds;

    public CheapSharkService(
            RestClient.Builder restClientBuilder,

            @Value("${cheapshark.api.base-url:https://www.cheapshark.com/api/1.0}")
            String cheapSharkBaseUrl,

            @Value("${cheapshark.redirect.base-url:https://www.cheapshark.com}")
            String cheapSharkRedirectBaseUrl,

            CheapSharkDealsCacheService dealsCacheService,
            CheapSharkGameCacheRepository gameCacheRepository,

            @Value("${cheapshark.cache.games.ttl-seconds:3600}")
            long gamesCacheTtlSeconds
    ) {

        // Normalizacja URL (usunięcie "/" na końcu)
        this.normalizedBaseUrl = cheapSharkBaseUrl.endsWith("/")
                ? cheapSharkBaseUrl.substring(0, cheapSharkBaseUrl.length() - 1)
                : cheapSharkBaseUrl;

        this.normalizedRedirectBaseUrl = cheapSharkRedirectBaseUrl.endsWith("/")
                ? cheapSharkRedirectBaseUrl.substring(0, cheapSharkRedirectBaseUrl.length() - 1)
                : cheapSharkRedirectBaseUrl;

        this.dealsCacheService = dealsCacheService;
        this.gameCacheRepository = gameCacheRepository;
        this.gamesCacheTtlSeconds = gamesCacheTtlSeconds;

        // Klient REST do komunikacji z API
        this.cheapSharkRestClient =
                restClientBuilder.baseUrl(this.normalizedBaseUrl).build();
    }

    /**
     * Pobranie ofert (z cache lub API).
     */
    public List<CheapSharkDealDto> getDeals(Integer storeId) {
        // Jeśli brak storeId → pobierz z domyślnych sklepów
        if (storeId == null) {
            return DEFAULT_STORE_IDS.stream().map(this::getDeals).flatMap(List::stream).toList();
        }

        // Próba pobrania z cache - jeśli brak w cache: pobranie i zapisanie w cache
        return dealsCacheService.getFreshDeals(storeId)
                .orElseGet(() -> fetchAndCacheDeals(storeId));
    }

    /**
     * Wymuszone odświeżenie cache ofert.
     */
    @SuppressWarnings("unused")
    public List<CheapSharkDealDto> refreshDeals(Integer storeId) {
        return fetchAndCacheDeals(storeId);
    }

    /**
     * Pobranie danych z API i zapis do cache.
     */
    private List<CheapSharkDealDto> fetchAndCacheDeals(Integer storeId) {

        List<CheapSharkDealDto> deals = cheapSharkRestClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/deals")
                                .queryParamIfPresent("storeID", Optional.ofNullable(storeId))
                                .build())
                        .retrieve()
                        .body(new org.springframework.core.ParameterizedTypeReference<>() {});

        dealsCacheService.saveDeals(storeId, deals);
        return deals;
    }

    /**
     * Wyszukiwanie gier po tytule.
     */
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

    /**
     * Pobranie szczegółów oferty po ID.
     */
    public CheapSharkDealDetailsDto getDealById(String dealId) {

        String normalizedDealId =
                normalizePossiblyEncodedValue(dealId);

        String encodedDealId =
                URLEncoder.encode(
                        normalizedDealId,
                        StandardCharsets.UTF_8
                );

        return cheapSharkRestClient.get()
                .uri(URI.create(
                        normalizedBaseUrl +
                                "/deals?id=" +
                                encodedDealId
                ))
                .retrieve()
                .body(CheapSharkDealDetailsDto.class);
    }

    /**
     * Pobranie szczegółów gry (z cache lub API).
     */
    public CheapSharkGameDetailsDto getGameById(String gameId) {

        String normalizedGameId =
                normalizePossiblyEncodedValue(gameId);

        // Sprawdzenie cache
        Optional<CheapSharkGameCacheDocument> cached =
                gameCacheRepository
                        .findByGameIdAndExpiresAtAfter(
                                normalizedGameId,
                                Instant.now()
                        );

        if (cached != null && cached.isPresent()) {
            return cached.get().getGameDetails();
        }

        String encodedGameId =
                URLEncoder.encode(
                        normalizedGameId,
                        StandardCharsets.UTF_8
                );

        CheapSharkGameDetailsDto details =
                cheapSharkRestClient.get()
                        .uri(URI.create(
                                normalizedBaseUrl +
                                        "/games?id=" +
                                        encodedGameId
                        ))
                        .retrieve()
                        .body(CheapSharkGameDetailsDto.class);

        // zapis do cache
        if (details != null) {
            Instant now = Instant.now();

            gameCacheRepository.save(
                    new CheapSharkGameCacheDocument(
                            buildGameCacheDocumentId(normalizedGameId),
                            normalizedGameId,
                            details,
                            now,
                            now.plusSeconds(gamesCacheTtlSeconds)
                    )
            );
        }

        return details;
    }

    /**
     * Generowanie URL przekierowania do sklepu.
     */
    public String buildRedirectUrl(String dealId) {

        String normalizedDealId =
                normalizePossiblyEncodedValue(dealId);

        String encodedDealId =
                URLEncoder.encode(
                        normalizedDealId,
                        StandardCharsets.UTF_8
                );

        return normalizedRedirectBaseUrl +
                "/redirect?dealID=" +
                encodedDealId;
    }

    /**
     * Obsługa zarówno raw jak i encoded ID.
     */
    private String normalizePossiblyEncodedValue(String value) {

        if (value == null || !value.contains("%")) {
            return value;
        }

        try {
            return UriUtils.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return value;
        }
    }

    /**
     * Generowanie klucza cache dla gry.
     */
    private String buildGameCacheDocumentId(String normalizedGameId) {

        return "v1:cheapshark:game:" + normalizedGameId;
    }
}