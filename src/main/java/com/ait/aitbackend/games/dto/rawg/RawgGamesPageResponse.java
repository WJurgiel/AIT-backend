package com.ait.aitbackend.games.dto.rawg;

import java.util.List;

public record RawgGamesPageResponse(
        List<RawgGameListItemDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}

