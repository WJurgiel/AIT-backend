package com.ait.aitbackend.notification.scheduler;

import com.ait.aitbackend.notification.service.WishlistSaleNotificationService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class WishlistSaleNotificationScheduler {
    private final WishlistSaleNotificationService wishlistSaleNotificationService;

    @Scheduled(
            fixedDelayString = "${notifications.wishlist-on-sale.scheduler-delay-ms:86400000}",
            initialDelayString = "${notifications.wishlist-on-sale.scheduler-delay-ms:86400000}"
    )
    /**
     * Cykliczne zadanie, wyzwalające serwis powiadomień o zniżkach na gry z listy życzeń i wpisujące liczbę udanych operacji do logów.
     */
    public void sendWishlistSaleNotificationsScheduled() {
        int sentEmails = wishlistSaleNotificationService.sendWishlistSaleNotifications();
        log.info("Wishlist sale notification run finished; sent {} email(s)", sentEmails);
    }
}

