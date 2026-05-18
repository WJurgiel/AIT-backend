package com.ait.aitbackend.notification.controller;

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
    private final WishlistSaleNotificationService wishlistSaleNotificationService;

    @PostMapping("/wishlist-sale/send")
    public ResponseEntity<NotificationResponse> sendWishlistSaleNotifications() {
        int sentEmails = wishlistSaleNotificationService.sendWishlistSaleNotifications();
        return ResponseEntity.ok(new NotificationResponse(
                sentEmails,
                "Sent " + sentEmails + " wishlist sale notification(s)"
        ));
    }

    @PostMapping("/wishlist-sale/test")
    public ResponseEntity<WishlistSaleNotificationService.TestEmailContent> getTestWishlistSaleEmailPreview(@RequestBody TestEmailRequest request) {
        var emailContent = wishlistSaleNotificationService.buildTestEmailContent(request.email());
        return ResponseEntity.ok(emailContent);
    }

    public record NotificationResponse(int count, String message) {
    }

    public record TestEmailRequest(String email) {
    }
}






