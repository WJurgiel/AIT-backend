package com.ait.aitbackend.games.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO mapujące szczegóły oferty z API CheapShark.
 * Zawiera informacje o grze, tańszych sklepach oraz historii ceny.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CheapSharkDealDetailsDto(

        GameInfoDto gameInfo,
        List<CheaperStoreDto> cheaperStores,
        CheapestPriceDto cheapestPrice
) {

    /**
     * Informacje o grze w ramach konkretnej oferty.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GameInfoDto(

            @JsonProperty("storeID")
            String storeId,

            @JsonProperty("gameID")
            String gameId,

            String name,

            @JsonProperty("steamAppID")
            String steamAppId,

            String salePrice,
            String retailPrice,
            String steamRatingText,
            String steamRatingPercent,
            String steamRatingCount,
            String metacriticScore,
            String metacriticLink,
            Long releaseDate,
            String publisher,
            String steamworks,
            String thumb
    ) {}

    /**
     * Informacja o tańszych ofertach w innych sklepach.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CheaperStoreDto(

            @JsonProperty("storeID")
            String storeId,

            @JsonProperty("dealID")
            String dealId,

            String salePrice,
            String retailPrice
    ) {}

    /**
     * Najniższa historyczna cena gry.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CheapestPriceDto(

            String price,
            Long date
    ) {}
}