package com.ait.aitbackend.games.dto.rawg;

public record RawgGameListItemDto(
        Integer rawgId,
        String name,
        String slug,
        String released,
        String backgroundImage,
        Double rating,
        Integer metacritic,
        String cheapsharkGameId
) {}

