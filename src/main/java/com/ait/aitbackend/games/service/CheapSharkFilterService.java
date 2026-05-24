package com.ait.aitbackend.games.service;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.ait.aitbackend.games.dto.cheapshark.DealsPageResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Serwis odpowiedzialny za filtrowanie, sortowanie i paginację ofert CheapShark.
 * Operuje na danych już pobranych (np. z cache), bez wpływu na logikę fetchowania.
 */
@Service
public class CheapSharkFilterService {

    /**
     * Filtruje, sortuje i paginuje listę ofert.
     */
    public DealsPageResponse filter(List<CheapSharkDealDto> deals, String search, Double minSavings, Double maxPrice,
            Double minRating, String sortBy, String sortDir, int page, int size) {

        // Filtrowanie danych w pamięci
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
        int toIndex = Math.min(fromIndex + size, total);

        List<CheapSharkDealDto> pageContent = filtered.subList(fromIndex, toIndex);

        return new DealsPageResponse(pageContent, page, size, total, totalPages, toIndex >= total);
    }

    // ─────────────────────────────────────────────
    // FILTRY
    // ─────────────────────────────────────────────

    private boolean matchesSearch(CheapSharkDealDto d, String search) {

        if (search == null || search.isBlank())
            return true;

        return d.title() != null &&
                d.title()
                        .toLowerCase(Locale.ROOT)
                        .contains(search.toLowerCase(Locale.ROOT));
    }

    private boolean matchesMinSavings(CheapSharkDealDto d, Double minSavings) {

        if (minSavings == null)
            return true;

        try {
            return Double.parseDouble(d.savings()) >= minSavings;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean matchesMaxPrice(CheapSharkDealDto d, Double maxPrice) {

        if (maxPrice == null)
            return true;

        try {
            return Double.parseDouble(d.salePrice()) <= maxPrice;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean matchesMinRating(CheapSharkDealDto d, Double minRating) {

        if (minRating == null)
            return true;

        try {
            return Double.parseDouble(d.steamRatingPercent()) >= minRating;
        } catch (Exception e) {
            return false;
        }
    }

    // ─────────────────────────────────────────────
    // SORTOWANIE
    // ─────────────────────────────────────────────

    private Comparator<CheapSharkDealDto> buildComparator(
            String sortBy,
            String sortDir
    ) {

        Comparator<CheapSharkDealDto> comparator = switch (
                sortBy == null
                        ? ""
                        : sortBy.toLowerCase(Locale.ROOT)
                ) {
            case "title" ->
                    Comparator.comparing(d -> nullSafeStr(d.title()));

            case "price" ->
                    Comparator.comparingDouble(
                            d -> parseDouble(d.salePrice())
                    );

            case "rating" ->
                    Comparator.comparingDouble(
                            d -> parseDouble(d.steamRatingPercent())
                    );

            default ->
                // domyślnie sortowanie po savings
                    Comparator.comparingDouble(
                            d -> parseDouble(d.savings())
                    );
        };

        boolean descending =
                !"asc".equalsIgnoreCase(sortDir);

        return descending ? comparator.reversed() : comparator;
    }

    // ─────────────────────────────────────────────
    // HELPERY
    // ─────────────────────────────────────────────

    private double parseDouble(String value) {

        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String nullSafeStr(String s) {
        return s == null ? "" : s;
    }
}