package com.ait.aitbackend.notification.controller;

import com.ait.aitbackend.notification.service.DailyDigestNotificationService;
import com.ait.aitbackend.notification.service.WishlistSaleNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private static final String DEFAULT_TEST_RECIPIENT = "wojtek123@example.com";

    private final DailyDigestNotificationService dailyDigestNotificationService;
    private final WishlistSaleNotificationService wishlistSaleNotificationService;

    @PostMapping("/wishlist-sale/send")
    public ResponseEntity<NotificationResponse> sendWishlistSaleNotifications() {
        int sentEmails = wishlistSaleNotificationService.sendWishlistSaleNotifications();
        return ResponseEntity.ok(new NotificationResponse(
                sentEmails,
                "Sent " + sentEmails + " wishlist sale notification(s)"
        ));
    }

    @PostMapping("/daily-digest/send")
    public ResponseEntity<NotificationResponse> sendDailyDigestNotifications() {
        int sentEmails = dailyDigestNotificationService.sendDailyDigestNotifications();
        return ResponseEntity.ok(new NotificationResponse(
                sentEmails,
                "Sent " + sentEmails + " daily digest notification(s)"
        ));
    }

    @PostMapping("/wishlist-sale/test")
    public ResponseEntity<WishlistSaleNotificationService.TestEmailContent> getTestWishlistSaleEmailPreview(@RequestBody(required = false) TestEmailRequest request) {
        String recipientEmail = resolveRecipientEmail(request);
        wishlistSaleNotificationService.sendTestWishlistSaleEmail(recipientEmail);
        var emailContent = wishlistSaleNotificationService.buildTestEmailContent(recipientEmail);
        return ResponseEntity.ok(emailContent);
    }

    private String resolveRecipientEmail(TestEmailRequest request) {
        if (request == null || request.email() == null) {
            return DEFAULT_TEST_RECIPIENT;
        }

        String email = request.email().trim();
        if (email.isBlank() || email.equalsIgnoreCase("string") || !email.contains("@") || email.endsWith("@")) {
            return DEFAULT_TEST_RECIPIENT;
        }

        return email;
    }

    public record NotificationResponse(int count, String message) {
    }

    public record TestEmailRequest(String email) {
    }
}
