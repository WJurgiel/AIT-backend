package com.ait.aitbackend.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "wishlist_sale_notifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wishlist_sale_notification",
                columnNames = {"user_id", "game_id", "deal_id", "sale_price"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistSaleNotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Column(name = "deal_id", nullable = false)
    private String dealId;

    @Column(name = "sale_price", nullable = false)
    private String salePrice;

    @Column(name = "notified_at", nullable = false)
    private Instant notifiedAt;

    public WishlistSaleNotificationHistory(Long userId, String gameId, String dealId, String salePrice, Instant notifiedAt) {
        this.userId = userId;
        this.gameId = gameId;
        this.dealId = dealId;
        this.salePrice = salePrice;
        this.notifiedAt = notifiedAt;
    }
}

