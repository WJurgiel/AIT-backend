package com.ait.aitbackend.user.dto;

import java.util.List;

public record UserPreferencesDto(
        List<String> platforms,
        NotificationsDto notifications
) {
    public record NotificationsDto(
            boolean wishlistOnSale,
            boolean dailyDigest,
            boolean flashSales,
            boolean priceDropAlerts
    ) {}
}
