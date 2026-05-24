package com.ait.aitbackend.notification.repository;

import com.ait.aitbackend.notification.entity.WishlistSaleNotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistSaleNotificationHistoryRepository extends JpaRepository<WishlistSaleNotificationHistory, Long> {
    boolean existsByUserIdAndGameIdAndDealIdAndSalePrice(Long userId, String gameId, String dealId, String salePrice);
}

