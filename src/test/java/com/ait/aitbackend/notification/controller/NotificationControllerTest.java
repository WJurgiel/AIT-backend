package com.ait.aitbackend.notification.controller;

import com.ait.aitbackend.notification.service.DailyDigestNotificationService;
import com.ait.aitbackend.notification.service.WishlistSaleNotificationService;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private DailyDigestNotificationService dailyDigestNotificationService;

    @Mock
    private WishlistSaleNotificationService wishlistSaleNotificationService;

    @InjectMocks
    private NotificationController notificationController;

    /**
     * Sprawdza, czy kontroler powiadomień o wyprzedażach z wishlisty zwraca poprawną liczbę wysłanych e-maili i odpowiedni komunikat.
     */
    @Test
    void shouldReturnSentEmailCount() {
        when(wishlistSaleNotificationService.sendWishlistSaleNotifications()).thenReturn(3);

        ResponseEntity<NotificationController.NotificationResponse> response = notificationController.sendWishlistSaleNotifications();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        NotificationController.NotificationResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(3, body.count());
        assertEquals("Sent 3 wishlist sale notification(s)", body.message());
    }

    /**
     * Upewnia się, że jeśli nie wysłano żadnych powiadomień o wyprzedażach, kontroler poprawnie zwraca zero i status 200 OK.
     */
    @Test
    void shouldReturnZeroWhenNoEmailsSent() {
        when(wishlistSaleNotificationService.sendWishlistSaleNotifications()).thenReturn(0);

        ResponseEntity<NotificationController.NotificationResponse> response = notificationController.sendWishlistSaleNotifications();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        NotificationController.NotificationResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(0, body.count());
        assertEquals("Sent 0 wishlist sale notification(s)", body.message());
    }

    /**
     * Weryfikuje działanie endpointu wysyłającego dzienne podsumowania, upewniając się, że zwraca on precyzyjną liczbę wygenerowanych e-maili
     */
    @Test
    void shouldReturnDailyDigestCount() {
        when(dailyDigestNotificationService.sendDailyDigestNotifications()).thenReturn(4);

        ResponseEntity<NotificationController.NotificationResponse> response = notificationController.sendDailyDigestNotifications();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        NotificationController.NotificationResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(4, body.count());
        assertEquals("Sent 4 daily digest notification(s)", body.message());
    }

    /**
     * Testuje endpoint e-maila testowego. Sprawdza, czy prawidłowo wysyła on wiadomość i zwraca jej poprawny podgląd bazujący na zmockowanych danych.
     */
    @Test
    void shouldSendTestEmailAndReturnPreview() {
        NotificationController.TestEmailRequest request = new NotificationController.TestEmailRequest("trap@mailtrap.io");
        WishlistSaleNotificationService.TestEmailContent content = new WishlistSaleNotificationService.TestEmailContent(
                "trap@mailtrap.io",
                "alerts@ait.local",
                "Twoje gry z listy życzeń są teraz na promocji",
                "Cześć Wojtek123,\n\nWykryliśmy nowe promocje dla gier z Twojej listy życzeń:\n\n- Devil May Cry 5 | cena promocyjna: $14.99 | cena regularna: $29.99 | oszczędzasz: 50.0% | link: https://www.cheapshark.com/redirect?dealID=dmc5-deal-test\n\nJeśli chcesz zmienić preferencje powiadomień, zrób to w swoim profilu."
        );

        doNothing().when(wishlistSaleNotificationService).sendTestWishlistSaleEmail("trap@mailtrap.io");
        when(wishlistSaleNotificationService.buildTestEmailContent("trap@mailtrap.io")).thenReturn(content);

        ResponseEntity<WishlistSaleNotificationService.TestEmailContent> response = notificationController.getTestWishlistSaleEmailPreview(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        WishlistSaleNotificationService.TestEmailContent body = response.getBody();
        assertNotNull(body);
        assertEquals(content, body);
        assertTrue(body.body().contains("Wojtek123"));
    }
}


