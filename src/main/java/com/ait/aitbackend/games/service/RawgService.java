package com.ait.aitbackend.games.service;

import com.ait.aitbackend.games.dto.rawg.RawgGamesResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Serwis integrujący się z API RAWG.
 * Odpowiada za wyszukiwanie gier oraz pobieranie szczegółów gry.
 */
@Service
public class RawgService {

    private final RestClient rawgRestClient;
    private final String rawgApiKey;

    public RawgService(
            RestClient.Builder restClientBuilder,

            @Value("${rawg.api.base-url:https://api.rawg.io/api}")
            String rawgBaseUrl,

            @Value("${rawg.api.key}")
            String rawgApiKey
    ) {
        // Klient HTTP skonfigurowany pod RAWG API
        this.rawgRestClient =
                restClientBuilder.baseUrl(rawgBaseUrl).build();

        this.rawgApiKey = rawgApiKey;
    }

    /**
     * Wyszukiwanie gier po nazwie.
     */
    public RawgGamesResponseDto searchGames(String search) {
        ensureRawgKeyConfigured();
        return rawgRestClient.get().uri(uriBuilder -> {
                    var uri = uriBuilder.path("/games").queryParam("key", rawgApiKey);

                    // opcjonalny parametr wyszukiwania
                    if (search != null && !search.isBlank())
                        uri = uri.queryParam("search", search);

                    return uri.build();
                }).retrieve().body(RawgGamesResponseDto.class);
    }

    /**
     * Pobranie szczegółów gry po ID.
     */
    public RawgGamesResponseDto.RawgGameDto getGameById(Integer id) {

        ensureRawgKeyConfigured();

        return rawgRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games/{id}")
                        .queryParam("key", rawgApiKey)
                        .build(id))
                .retrieve()
                .body(RawgGamesResponseDto.RawgGameDto.class);
    }

    /**
     * Walidacja konfiguracji API KEY.
     */
    private void ensureRawgKeyConfigured() {

        if (rawgApiKey == null || rawgApiKey.isBlank()) {
            throw new IllegalStateException(
                    "RAWG API key is not configured. " + "Set RAWG_TOKEN in .env or RAWG_API_KEY in environment."
            );
        }
    }
}