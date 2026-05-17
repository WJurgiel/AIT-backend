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

    public RawgGamesResponseDto searchGames(Integer storeId, String search) {
        return rawgRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam("key", rawgApiKey)
                        .queryParam("stores", storeId)
                        .queryParam("search", search)
                        .build())
                .retrieve()
                .body(RawgGamesResponseDto.class);
    }

    /**
     * Get all games from RAWG API with pagination
     * Used for caching all games from specified stores
     *
     * @param stores Comma-separated store IDs (e.g., "1,5,11" for Steam, GoG, Epic Games)
     * @param pageSize Number of results per page
     * @param pageNumber Page number (1-indexed in RAWG API)
     * @return Response containing games for the requested page
     */
    public RawgGamesResponseDto getAllGames(String stores, int pageSize, int pageNumber) {
        return rawgRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam("key", rawgApiKey)
                        .queryParam("stores", stores)
                        .queryParam("page_size", pageSize)
                        .queryParam("page", pageNumber)
                        .build())
                .retrieve()
                .body(RawgGamesResponseDto.class);
    }
}

