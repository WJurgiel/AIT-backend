package com.ait.aitbackend.games.controller;

import com.ait.aitbackend.games.dto.rawg.RawgGamesResponseDto;
import com.ait.aitbackend.games.service.RawgService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/rawg")
@AllArgsConstructor
public class RawgController {
    private final RawgService rawgService;

    @GetMapping(value = "/games", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RawgGamesResponseDto> searchGames(
            @RequestParam(name = "search", required = false) String search
    ) {
        return ResponseEntity.ok(rawgService.searchGames(search));
    }

    @GetMapping(value = "/games/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RawgGamesResponseDto.RawgGameDto> getGameById(@PathVariable Integer id) {
        return ResponseEntity.ok(rawgService.getGameById(id));
    }
}

