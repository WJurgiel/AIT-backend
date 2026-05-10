package com.ait.aitbackend.games.dto.cheapshark;

import java.util.List;

public record DealsPageResponse(
        List<CheapSharkDealDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}
