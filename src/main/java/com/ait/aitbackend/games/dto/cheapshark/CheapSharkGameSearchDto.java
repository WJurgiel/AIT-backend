package com.ait.aitbackend.games.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheapSharkGameSearchDto(
        @JsonProperty("gameID") String gameId,
        @JsonProperty("steamAppID") String steamAppId,
        String cheapest,
        @JsonProperty("cheapestDealID") String cheapestDealId,
        String external,
        String internalName,
        String thumb
) {
}

