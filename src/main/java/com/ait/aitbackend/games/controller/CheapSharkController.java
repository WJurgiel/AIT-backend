package com.ait.aitbackend.games.controller;

import com.ait.aitbackend.games.dto.GameDetailsDto;
import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.ait.aitbackend.games.dto.cheapshark.DealsPageResponse;
import com.ait.aitbackend.games.service.CheapSharkFilterService;
import com.ait.aitbackend.games.service.CheapSharkService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Kontroler odpowiedzialny za integrację z CheapShark
 * oraz przygotowanie danych dla frontendowego widoku szczegółów gry.
 */
@RestController
@RequestMapping("/api/cheapshark")
@AllArgsConstructor
public class CheapSharkController {

    private final CheapSharkService cheapSharkService;
    private final CheapSharkFilterService filterService;

    /**
     * Pobranie listy ofert z filtrowaniem, sortowaniem i paginacją.
     */
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
            @RequestParam(defaultValue = "desc") String sortDir) {

        List<CheapSharkDealDto> allDeals = cheapSharkService.getDeals(platformId);

        DealsPageResponse response = filterService.filter(allDeals, search, minSavings, maxPrice,
                        minRating, sortBy, sortDir, page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * Szczegóły gry (mapowanie CheapShark → DTO frontendowe).
     */
    @GetMapping(value = "/game/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GameDetailsDto> getGameDetails(@RequestParam("id") String dealId) {

        var details = cheapSharkService.getDealById(dealId);

        // Nazwa platformy
        String platformLabel = switch (details.gameInfo().storeId()) {
            case "1" -> "Steam";
            case "7" -> "GOG";
            case "25" -> "Epic";
            default -> "Store " + details.gameInfo().storeId();
        };

        // Link do sklepu
        String redirect = cheapSharkService.buildRedirectUrl(dealId);

        // Data wydania (fallback jeśli brak)
        String release = details.gameInfo().releaseDate() == null || details.gameInfo().releaseDate() == 0 ? "Unknown"
                        : Instant.ofEpochSecond(details.gameInfo().releaseDate()).toString();

        // Obliczenie oszczędności
        String savings = "0.00";
        try {
            double retail = Double.parseDouble(details.gameInfo().retailPrice());
            double sale = Double.parseDouble(details.gameInfo().salePrice());
            savings = String.format("%.2f", Math.max(retail - sale, 0.0));
        } catch (Exception ignored) {}

        var prices = new GameDetailsDto.PriceDto(details.gameInfo().retailPrice(), details.gameInfo().salePrice(), savings);

        // inne oferty (jeśli istnieją)
        List<GameDetailsDto.OtherOfferDto> otherOffers;

        if (details.cheaperStores() == null) {
            otherOffers = List.of();
        } else {
            otherOffers = details.cheaperStores().stream().map(cs -> {
                        String otherPlatform = "Store " + cs.storeId();
                        String otherRedirect = cheapSharkService.buildRedirectUrl(cs.dealId());
                        return new GameDetailsDto.OtherOfferDto(otherPlatform, cs.retailPrice(), cs.salePrice(), otherRedirect);
                    }).toList();
        }

        var dto = new GameDetailsDto(details.gameInfo().name(),null, platformLabel, prices,
                details.gameInfo().steamRatingPercent(), details.gameInfo().thumb(), release, redirect, otherOffers);

        return ResponseEntity.ok(dto);
    }

    /**
     * Przekierowanie do sklepu z ofertą.
     */
    @GetMapping("/redirect")
    public ResponseEntity<Void> redirectToStore(@RequestParam("dealID") String dealId) {
        String redirectUrl = cheapSharkService.buildRedirectUrl(dealId);

        return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();
    }
}