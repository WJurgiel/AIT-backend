package com.ait.aitbackend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabela przechowująca dane preferencji użytkownika.
 * Zawiera preferowane platformy oraz opcje notyfikacji.
 */
@Embeddable
@Getter
@Setter
public class UserPreferences {

    @Column(name = "pref_platforms")
    private String platforms = "";

    @Column(name = "notif_wishlist_on_sale")
    private boolean wishlistOnSale = true;

    @Column(name = "notif_daily_digest")
    private boolean dailyDigest = true;

    @Column(name = "notif_flash_sales")
    private boolean flashSales = false;

    @Column(name = "notif_price_drop_alerts")
    private boolean priceDropAlerts = true;

    public List<String> getPlatformList() {
        if (platforms == null || platforms.isBlank()) return new ArrayList<>();
        return new ArrayList<>(List.of(platforms.split(",")));
    }

    public void setPlatformList(List<String> list) {
        this.platforms = list == null ? "" : String.join(",", list);
    }

}
