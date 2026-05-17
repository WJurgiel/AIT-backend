package com.ait.aitbackend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Embeddable
@Getter
@Setter
public class UserPreferences {

    @Column(name = "pref_platforms")
    private String platforms = "";

    @Column(name = "pref_genres")
    private String genres = "";

    @Column(name = "notif_wishlist_on_sale")
    private boolean wishlistOnSale = true;

    @Column(name = "notif_daily_digest")
    private boolean dailyDigest = true;

    @Column(name = "notif_flash_sales")
    private boolean flashSales = false;

    @Column(name = "notif_price_drop_alerts")
    private boolean priceDropAlerts = true;

    /**
     * Comma-separated list of watched/favorite game IDs from CheapShark API
     * These are the games user wants to be notified about when they go on sale
     */
    @Column(name = "watched_game_ids", columnDefinition = "TEXT")
    private String watchedGameIds = "";

    public List<String> getPlatformList() {
        if (platforms == null || platforms.isBlank()) return new ArrayList<>();
        return new ArrayList<>(List.of(platforms.split(",")));
    }

    public void setPlatformList(List<String> list) {
        this.platforms = list == null ? "" : String.join(",", list);
    }

    public List<String> getGenreList() {
        if (genres == null || genres.isBlank()) return new ArrayList<>();
        return new ArrayList<>(List.of(genres.split(",")));
    }

    public void setGenreList(List<String> list) {
        this.genres = list == null ? "" : String.join(",", list);
    }

    /**
     * Get list of watched game IDs
     */
    public List<String> getWatchedGameIdList() {
        if (watchedGameIds == null || watchedGameIds.isBlank()) return new ArrayList<>();
        return new ArrayList<>(List.of(watchedGameIds.split(",")));
    }

    /**
     * Set list of watched game IDs
     */
    public void setWatchedGameIdList(List<String> list) {
        if (list == null || list.isEmpty()) {
            this.watchedGameIds = "";
            return;
        }

        this.watchedGameIds = list.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    /**
     * Add a game ID to watched list if not already present
     */
    public boolean addWatchedGameId(String gameId) {
        if (gameId == null || gameId.isBlank()) {
            return false;
        }
        String normalizedGameId = gameId.trim();
        List<String> gameIds = getWatchedGameIdList();
        if (!gameIds.contains(normalizedGameId)) {
            gameIds.add(normalizedGameId);
            setWatchedGameIdList(gameIds);
            return true;
        }
        return false;
    }

    /**
     * Remove a game ID from watched list
     */
    public boolean removeWatchedGameId(String gameId) {
        if (gameId == null || gameId.isBlank()) {
            return false;
        }
        String normalizedGameId = gameId.trim();
        List<String> gameIds = getWatchedGameIdList();
        boolean removed = gameIds.remove(normalizedGameId);
        if (removed) {
            setWatchedGameIdList(gameIds);
        }
        return removed;
    }

    /**
     * Check if a game ID is in watched list
     */
    public boolean isGameIdWatched(String gameId) {
        if (gameId == null || gameId.isBlank()) {
            return false;
        }
        return getWatchedGameIdList().contains(gameId.trim());
    }
}
