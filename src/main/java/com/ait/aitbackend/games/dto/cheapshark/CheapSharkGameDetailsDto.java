package com.ait.aitbackend.games.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO mapujące szczegóły gry z API CheapShark.
 * Zawiera informacje o grze, historii ceny oraz dostępnych ofertach.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CheapSharkGameDetailsDto(

        InfoDto info,
        CheapestPriceEverDto cheapestPriceEver,
        List<DealDto> deals
) {

    /**
     * Podstawowe informacje o grze.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InfoDto(

            String title,

            @JsonProperty("steamAppID")
            String steamAppId,

            String thumb
    ) {}

    /**
     * Najniższa historyczna cena gry.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CheapestPriceEverDto(

            String price,
            Long date
    ) {}

    /**
     * Informacje o dostępnych ofertach (dealach).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DealDto(

            @JsonProperty("storeID")
            String storeId,

            @JsonProperty("dealID")
            String dealId,

            String price,
            String retailPrice,
            String savings
    ) {}
}