package com.ait.aitbackend.games.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheapSharkGameDetailsDto(
        InfoDto info,
        CheapestPriceEverDto cheapestPriceEver,
        List<DealDto> deals
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InfoDto(
            String title,
            @JsonProperty("steamAppID") String steamAppId,
            String thumb
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CheapestPriceEverDto(
            String price,
            Long date
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DealDto(
            @JsonProperty("storeID") String storeId,
            @JsonProperty("dealID") String dealId,
            String price,
            String retailPrice,
            String savings
    ) {
    }
}
