package com.ait.aitbackend.notification.scheduler;

import com.ait.aitbackend.notification.service.WishlistSaleNotificationService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WishlistSaleNotificationSchedulerTest {

    /**
     * Weryfikuje, czy scheduler odpowiedzialny za powiadomienia o wyprzedażach z wishlisty bezbłędnie uruchamia odpowiednią metodę serwisu
     */
    @Test
    void shouldDelegateToNotificationService() {
        WishlistSaleNotificationService service = mock(WishlistSaleNotificationService.class);
        WishlistSaleNotificationScheduler scheduler = new WishlistSaleNotificationScheduler(service);

        scheduler.sendWishlistSaleNotificationsScheduled();

        verify(service).sendWishlistSaleNotifications();
    }
}

