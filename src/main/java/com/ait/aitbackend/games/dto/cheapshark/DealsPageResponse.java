package com.ait.aitbackend.games.dto.cheapshark;

import java.util.List;

/**
 * DTO reprezentujące stronę wyników (pagination) dla ofert CheapShark.
 */
public record DealsPageResponse(
        List<CheapSharkDealDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}
