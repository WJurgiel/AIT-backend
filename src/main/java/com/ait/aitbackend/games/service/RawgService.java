package com.ait.aitbackend.games.service;

import com.ait.aitbackend.games.dto.rawg.RawgGamesResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RawgService {
    private final RestClient rawgRestClient;
    private final String rawgApiKey;

    public RawgService(
            RestClient.Builder restClientBuilder,
            @Value("${rawg.api.base-url:https://api.rawg.io/api}") String rawgBaseUrl,
            @Value("${rawg.api.key}") String rawgApiKey
    ) {
        this.rawgRestClient = restClientBuilder.baseUrl(rawgBaseUrl).build();
        this.rawgApiKey = rawgApiKey;
    }

    public RawgGamesResponseDto searchGames(String search) {
        ensureRawgKeyConfigured();
        return rawgRestClient.get()
                .uri(uriBuilder -> {
                    var uri = uriBuilder.path("/games").queryParam("key", rawgApiKey);
                    if (search != null && !search.isBlank()) uri = uri.queryParam("search", search);
                    return uri.build();
                })
                .retrieve()
                .body(RawgGamesResponseDto.class);
    }

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

    private void ensureRawgKeyConfigured() {
        if (rawgApiKey == null || rawgApiKey.isBlank()) {
            throw new IllegalStateException("RAWG API key is not configured. Set RAWG_TOKEN in .env or RAWG_API_KEY in the environment.");
        }
    }
}

