package com.ait.aitbackend.games.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheapSharkDealDto(
        String internalName,
        String title,
        String metacriticLink,
        @JsonProperty("dealID") String dealId,
        @JsonProperty("storeID") String storeId,
        @JsonProperty("gameID") String gameId,
        String salePrice,
        String normalPrice,
        String isOnSale,
        String savings,
        String metacriticScore,
        String steamRatingText,
        String steamRatingPercent,
        String steamRatingCount,
        @JsonProperty("steamAppID") String steamAppId,
        Long releaseDate,
        Long lastChange,
        String dealRating,
        String thumb
) {
}

