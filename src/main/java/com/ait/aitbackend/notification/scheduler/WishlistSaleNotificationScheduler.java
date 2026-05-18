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
            fixedDelayString = "${notifications.wishlist-on-sale.scheduler-delay-ms:3600000}",
            initialDelayString = "${notifications.wishlist-on-sale.scheduler-delay-ms:3600000}"
    )
    public void sendWishlistSaleNotificationsScheduled() {
        int sentEmails = wishlistSaleNotificationService.sendWishlistSaleNotifications();
        log.info("Wishlist sale notification run finished; sent {} email(s)", sentEmails);
    }
}

