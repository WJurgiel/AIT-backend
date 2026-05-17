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
            @RequestParam(value = "platformId", required = false) Integer platformId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minSavings,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "savings") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        List<CheapSharkDealDto> allDeals = cheapSharkService.getDeals(platformId);

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

    @GetMapping(value = "/game/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<com.ait.aitbackend.games.dto.GameDetailsDto> getGameDetails(@RequestParam("id") String dealId) {
        var details = cheapSharkService.getDealById(dealId);

        String platformLabel = switch (details.gameInfo().storeId()) {
            case "1" -> "Steam";
            case "7" -> "GOG";
            case "25" -> "Epic";
            default -> "Store " + details.gameInfo().storeId();
        };

        String redirect = cheapSharkService.buildRedirectUrl(dealId);

        String release = details.gameInfo().releaseDate() == null || details.gameInfo().releaseDate() == 0
                ? "Unknown"
                : java.time.Instant.ofEpochSecond(details.gameInfo().releaseDate()).toString();

        String savings = "0.00";
        try {
            double retail = Double.parseDouble(details.gameInfo().retailPrice());
            double sale = Double.parseDouble(details.gameInfo().salePrice());
            savings = String.format("%.2f", Math.max(retail - sale, 0.0));
        } catch (Exception ignored) {}

        var prices = new com.ait.aitbackend.games.dto.GameDetailsDto.PriceDto(
                details.gameInfo().retailPrice(),
                details.gameInfo().salePrice(),
                savings
        );

        java.util.List<com.ait.aitbackend.games.dto.GameDetailsDto.OtherOfferDto> otherOffers;
        if (details.cheaperStores() == null) {
            otherOffers = java.util.List.of();
        } else {
            otherOffers = details.cheaperStores().stream().map(cs -> {
                String otherPlatform = "Store " + cs.storeId();
                String otherRedirect = cheapSharkService.buildRedirectUrl(cs.dealId());
                return new com.ait.aitbackend.games.dto.GameDetailsDto.OtherOfferDto(
                        otherPlatform,
                        cs.retailPrice(),
                        cs.salePrice(),
                        otherRedirect
                );
            }).toList();
        }

        var dto = new com.ait.aitbackend.games.dto.GameDetailsDto(
                details.gameInfo().name(),
                null,
                platformLabel,
                prices,
                details.gameInfo().steamRatingPercent(),
                details.gameInfo().thumb(),
                release,
                redirect,
                otherOffers
        );

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/redirect")
    public ResponseEntity<Void> redirectToStore(@RequestParam("dealID") String dealId) {
        String redirectUrl = cheapSharkService.buildRedirectUrl(dealId);
        return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();
    }
}
