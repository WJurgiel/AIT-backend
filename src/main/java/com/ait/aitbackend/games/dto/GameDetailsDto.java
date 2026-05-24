package com.ait.aitbackend.games.dto;

import java.util.List;

/**
 * DTO reprezentujące szczegóły gry wykorzystywane w warstwie API.
 */
public record GameDetailsDto(

        String name,
        String description,
        String platform,
        PriceDto prices,
        String rating,
        String image,
        String releaseDate,
        String redirectUrl,
        List<OtherOfferDto> cheaperStores
) {

    /**
     * Informacje o cenach gry.
     */
    public record PriceDto(
            String retail,
            String sale,
            String savings
    ) {}

    /**
     * Alternatywne oferty z innych sklepów.
     */
    public record OtherOfferDto(
            String platform,
            String retail,
            String sale,
            String redirectUrl
    ) {}
}