package com.ait.aitbackend.notification.controller;

import com.ait.aitbackend.notification.service.WishlistSaleNotificationService;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private WishlistSaleNotificationService wishlistSaleNotificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void shouldReturnSentEmailCount() {
        when(wishlistSaleNotificationService.sendWishlistSaleNotifications()).thenReturn(3);

        ResponseEntity<NotificationController.NotificationResponse> response = notificationController.sendWishlistSaleNotifications();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().count());
        assertEquals("Sent 3 wishlist sale notification(s)", response.getBody().message());
    }

    @Test
    void shouldReturnZeroWhenNoEmailsSent() {
        when(wishlistSaleNotificationService.sendWishlistSaleNotifications()).thenReturn(0);

        ResponseEntity<NotificationController.NotificationResponse> response = notificationController.sendWishlistSaleNotifications();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().count());
        assertEquals("Sent 0 wishlist sale notification(s)", response.getBody().message());
    }
}


