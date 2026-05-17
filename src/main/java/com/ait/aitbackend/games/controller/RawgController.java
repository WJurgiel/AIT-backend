package com.ait.aitbackend.games.controller;

import com.ait.aitbackend.games.cache.RawgGamesCacheService;
import com.ait.aitbackend.games.dto.rawg.RawgGamesPageResponse;
import com.ait.aitbackend.games.dto.rawg.RawgGamesResponseDto;
import com.ait.aitbackend.games.service.RawgService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/rawg")
@AllArgsConstructor
public class RawgController {
    private final RawgService rawgService;
    private final RawgGamesCacheService rawgGamesCacheService;

    @GetMapping(value = "/games", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RawgGamesResponseDto> searchGames(
            @RequestParam("stores") Integer storeId,
            @RequestParam String search
    ) {
        return ResponseEntity.ok(rawgService.searchGames(storeId, search));
    }

    @GetMapping(value = "/games/cached", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RawgGamesPageResponse> getCachedGames(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        var pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(rawgGamesCacheService.getCachedGamesPage(search, pageable));
    }
}

