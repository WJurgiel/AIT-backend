package com.ait.aitbackend.games.controller;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDetailsDto;
import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.ait.aitbackend.games.dto.cheapshark.CheapSharkGameDetailsDto;
import com.ait.aitbackend.games.dto.cheapshark.CheapSharkGameSearchDto;
import com.ait.aitbackend.games.dto.cheapshark.DealsPageResponse;
import com.ait.aitbackend.games.service.CheapSharkFilterService;
import com.ait.aitbackend.games.service.CheapSharkService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cheapshark")
@AllArgsConstructor
public class CheapSharkController {

    private final CheapSharkService cheapSharkService;
    private final CheapSharkFilterService filterService;

    @GetMapping(value = "/deals", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DealsPageResponse> getDeals(
            @RequestParam(value = "storeID", required = false) Integer storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minSavings,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "savings") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        List<CheapSharkDealDto> allDeals = cheapSharkService.getDeals(storeId);

        DealsPageResponse response = filterService.filter(
                allDeals, search, minSavings, maxPrice, minRating, sortBy, sortDir, page, size
        );

        return ResponseEntity.ok(response);
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
