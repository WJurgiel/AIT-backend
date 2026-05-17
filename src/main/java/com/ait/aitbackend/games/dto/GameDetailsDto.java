package com.ait.aitbackend.games.dto;

import java.util.List;

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
    public record PriceDto(String retail, String sale, String savings) {}

    public record OtherOfferDto(String platform, String retail, String sale, String redirectUrl) {}
}

