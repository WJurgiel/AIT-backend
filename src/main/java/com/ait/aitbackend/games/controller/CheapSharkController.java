package com.ait.aitbackend.games.controller;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDetailsDto;
import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.ait.aitbackend.games.dto.cheapshark.CheapSharkGameDetailsDto;
import com.ait.aitbackend.games.dto.cheapshark.CheapSharkGameSearchDto;
import com.ait.aitbackend.games.service.CheapSharkService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cheapshark")
@AllArgsConstructor
public class CheapSharkController {
    private final CheapSharkService cheapSharkService;

    @GetMapping(value = "/deals", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CheapSharkDealDto>> getDeals(
            @RequestParam("storeID") Integer storeId,
            @RequestParam Integer onSale
    ) {
        return ResponseEntity.ok(cheapSharkService.getDeals(storeId, onSale));
    }

    @GetMapping(value = "/games", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CheapSharkGameSearchDto>> searchGames(@RequestParam String title) {
        return ResponseEntity.ok(cheapSharkService.searchGamesByTitle(title));
    }

    @GetMapping(value = "/deal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CheapSharkDealDetailsDto> getDealById(@RequestParam("id") String dealId) {
        return ResponseEntity.ok(cheapSharkService.getDealById(dealId));
    }

    @GetMapping(value = "/game", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CheapSharkGameDetailsDto> getGameById(@RequestParam("id") String gameId) {
        return ResponseEntity.ok(cheapSharkService.getGameById(gameId));
    }

    @GetMapping("/redirect")
    public ResponseEntity<Void> redirectToStore(@RequestParam("dealID") String dealId) {
        String redirectUrl = cheapSharkService.buildRedirectUrl(dealId);
        return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();
    }
}

