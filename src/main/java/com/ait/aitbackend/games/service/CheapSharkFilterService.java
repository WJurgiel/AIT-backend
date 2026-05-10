package com.ait.aitbackend.games.service;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.ait.aitbackend.games.dto.cheapshark.DealsPageResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Applies in-memory filtering and sorting to a list of CheapShark deals,
 * then paginates the result.

 * This is intentionally separate from CheapSharkService so the cache/fetch
 * logic stays untouched.
 */
@Service
public class CheapSharkFilterService {

    /**
     * @param deals       full list fetched from cache
     * @param search      optional title substring (case-insensitive)
     * @param minSavings  optional minimum savings % (0–100)
     * @param maxPrice    optional maximum sale price
     * @param minRating   optional minimum Steam rating % (0–100)
     * @param sortBy      title | price | savings | rating  (default: savings)
     * @param sortDir     asc | desc                        (default: desc)
     * @param page        0-based page index
     * @param size        page size
     */
    public DealsPageResponse filter(
            List<CheapSharkDealDto> deals,
            String search,
            Double minSavings,
            Double maxPrice,
            Double minRating,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        List<CheapSharkDealDto> filtered = deals.stream()
                .filter(d -> matchesSearch(d, search))
                .filter(d -> matchesMinSavings(d, minSavings))
                .filter(d -> matchesMaxPrice(d, maxPrice))
                .filter(d -> matchesMinRating(d, minRating))
                .sorted(buildComparator(sortBy, sortDir))
                .toList();

        int total = filtered.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 1;
        int fromIndex = Math.min(page * size, total);
        int toIndex   = Math.min(fromIndex + size, total);

        List<CheapSharkDealDto> pageContent = filtered.subList(fromIndex, toIndex);

        return new DealsPageResponse(
                pageContent,
                page,
                size,
                total,
                totalPages,
                toIndex >= total
        );
    }

    // ── predicates ───────────────────────────────────────────────────────────

    private boolean matchesSearch(CheapSharkDealDto d, String search) {
        if (search == null || search.isBlank()) return true;
        return d.title() != null &&
               d.title().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }

    private boolean matchesMinSavings(CheapSharkDealDto d, Double minSavings) {
        if (minSavings == null) return true;
        try {
            return Double.parseDouble(d.savings()) >= minSavings;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    private boolean matchesMaxPrice(CheapSharkDealDto d, Double maxPrice) {
        if (maxPrice == null) return true;
        try {
            return Double.parseDouble(d.salePrice()) <= maxPrice;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    private boolean matchesMinRating(CheapSharkDealDto d, Double minRating) {
        if (minRating == null) return true;
        try {
            return Double.parseDouble(d.steamRatingPercent()) >= minRating;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    // ── sorting ──────────────────────────────────────────────────────────────

    private Comparator<CheapSharkDealDto> buildComparator(String sortBy, String sortDir) {
        Comparator<CheapSharkDealDto> comparator = switch (sortBy == null ? "" : sortBy.toLowerCase(Locale.ROOT)) {
            case "title"   -> Comparator.comparing(d -> nullSafeStr(d.title()));
            case "price"   -> Comparator.comparingDouble(d -> parseDouble(d.salePrice()));
            case "rating"  -> Comparator.comparingDouble(d -> parseDouble(d.steamRatingPercent()));
            default        -> Comparator.comparingDouble(d -> parseDouble(d.savings())); // savings desc
        };

        boolean descending = !"asc".equalsIgnoreCase(sortDir);
        return descending ? comparator.reversed() : comparator;
    }

    private double parseDouble(String value) {
        try { return Double.parseDouble(value); }
        catch (NumberFormatException | NullPointerException e) { return 0.0; }
    }

    private String nullSafeStr(String s) {
        return s == null ? "" : s;
    }
}
