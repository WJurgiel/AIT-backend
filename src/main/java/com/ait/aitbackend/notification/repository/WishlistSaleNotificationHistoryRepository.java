package com.ait.aitbackend.notification.repository;

import com.ait.aitbackend.notification.entity.WishlistSaleNotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistSaleNotificationHistoryRepository extends JpaRepository<WishlistSaleNotificationHistory, Long> {
    /**
     * Weryfikuje w bazie danych, czy konkretny użytkownik został już powiadomiony o danej grze w ramach określonego identyfikatora oferty i konkretnej ceny.
     * @param userId
     * @param gameId
     * @param dealId
     * @param salePrice
     */
    boolean existsByUserIdAndGameIdAndDealIdAndSalePrice(Long userId, String gameId, String dealId, String salePrice);
}

