package com.ait.aitbackend.notification.service;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.ait.aitbackend.games.service.CheapSharkService;
import com.ait.aitbackend.notification.entity.WishlistSaleNotificationHistory;
import com.ait.aitbackend.notification.repository.WishlistSaleNotificationHistoryRepository;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WishlistSaleNotificationService {
    private final UserProfileRepository userProfileRepository;
    private final CheapSharkService cheapSharkService;
    private final JavaMailSender mailSender;
    private final WishlistSaleNotificationHistoryRepository historyRepository;
    private final String fromAddress;

    public WishlistSaleNotificationService(
            UserProfileRepository userProfileRepository,
            CheapSharkService cheapSharkService,
            JavaMailSender mailSender,
            WishlistSaleNotificationHistoryRepository historyRepository,
            @Value("${notifications.wishlist-on-sale.from:no-reply@ait.local}") String fromAddress
    ) {
        this.userProfileRepository = userProfileRepository;
        this.cheapSharkService = cheapSharkService;
        this.mailSender = mailSender;
        this.historyRepository = historyRepository;
        this.fromAddress = fromAddress;
    }

    @Transactional
    public int sendWishlistSaleNotifications() {
        Map<String, CheapSharkDealDto> bestDealsByGameId = cheapSharkService.getDeals(null).stream()
                .filter(this::isOnSale)
                .filter(deal -> deal.gameId() != null && !deal.gameId().isBlank())
                .collect(Collectors.toMap(
                        deal -> normalize(deal.gameId()),
                        Function.identity(),
                        this::pickBetterDeal,
                        LinkedHashMap::new
                ));

        int sentEmails = 0;
        for (UserProfile user : userProfileRepository.findAll()) {
            if (!shouldNotify(user)) {
                continue;
            }

            List<SaleAlert> alerts = buildAlertsForUser(user, bestDealsByGameId);
            if (alerts.isEmpty()) {
                continue;
            }

            sendEmail(user, alerts);
            historyRepository.saveAll(alerts.stream()
                    .map(alert -> new WishlistSaleNotificationHistory(
                            user.getId(),
                            alert.gameId(),
                            alert.deal().dealId(),
                            alert.deal().salePrice(),
                            Instant.now()
                    ))
                    .toList());
            sentEmails++;
        }

        return sentEmails;
    }

    private boolean shouldNotify(UserProfile user) {
        return user != null
                && user.getEmail() != null
                && !user.getEmail().isBlank()
                && user.getPreferences() != null
                && user.getPreferences().isWishlistOnSale();
    }

    private List<SaleAlert> buildAlertsForUser(UserProfile user, Map<String, CheapSharkDealDto> bestDealsByGameId) {
        Set<String> favoriteGameIds = normalizeFavorites(user);
        if (favoriteGameIds.isEmpty()) {
            return List.of();
        }

        List<SaleAlert> alerts = new ArrayList<>();
        for (String favoriteGameId : favoriteGameIds) {
            CheapSharkDealDto deal = bestDealsByGameId.get(favoriteGameId);
            if (deal == null) {
                continue;
            }

            if (historyRepository.existsByUserIdAndGameIdAndDealIdAndSalePrice(
                    user.getId(),
                    favoriteGameId,
                    normalize(deal.dealId()),
                    deal.salePrice()
            )) {
                continue;
            }

            alerts.add(new SaleAlert(favoriteGameId, deal));
        }

        alerts.sort(Comparator
                .comparing((SaleAlert alert) -> toDecimal(alert.deal().salePrice()))
                .thenComparing(alert -> toDecimal(alert.deal().dealRating()), Comparator.reverseOrder())
                .thenComparing(alert -> safeValue(alert.deal().title())));

        return alerts;
    }

    private Set<String> normalizeFavorites(UserProfile user) {
        if (user.getPreferences() == null) {
            return Set.of();
        }

        List<String> favorites = user.getPreferences().getFavoriteGameIdsList();
        if (favorites == null || favorites.isEmpty()) {
            return Set.of();
        }

        return favorites.stream()
                .map(this::normalize)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void sendEmail(UserProfile user, List<SaleAlert> alerts) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("Twoje gry z listy życzeń są teraz na promocji");
        message.setText(buildMessageText(user, alerts));
        sendEmailSafely(message, user.getEmail());
    }

    private void sendEmailSafely(SimpleMailMessage message, String recipientEmail) {
        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send email to {}. Message will be logged. Error: {}", recipientEmail, ex.getMessage());
            log.debug("Email content that would have been sent:", ex);
            log.info("EMAIL_LOG: To={}, Subject={}, Body preview={}",
                    recipientEmail,
                    message.getSubject(),
                    message.getText() != null && message.getText().length() > 100 ?
                        message.getText().substring(0, 100) + "..." :
                        message.getText());
        }
    }

    private String buildMessageText(UserProfile user, List<SaleAlert> alerts) {
        StringBuilder builder = new StringBuilder();
        builder.append("Cześć ").append(user.getUsername()).append(",\n\n");
        builder.append("Wykryliśmy nowe promocje dla gier z Twojej listy życzeń:\n\n");

        for (SaleAlert alert : alerts) {
            CheapSharkDealDto deal = alert.deal();
            builder.append("- ")
                    .append(safeValue(deal.title()))
                    .append(" | cena promocyjna: $").append(safeValue(deal.salePrice()))
                    .append(" | cena regularna: $").append(safeValue(deal.normalPrice()))
                    .append(" | oszczędzasz: ").append(safeValue(deal.savings())).append("%")
                    .append(" | link: ").append(cheapSharkService.buildRedirectUrl(deal.dealId()))
                    .append("\n");
        }

        builder.append("\nJeśli chcesz zmienić preferencje powiadomień, zrób to w swoim profilu.");
        return builder.toString();
    }

    private boolean isOnSale(CheapSharkDealDto deal) {
        if (deal == null) {
            return false;
        }

        if (isTruthy(deal.isOnSale())) {
            return true;
        }

        BigDecimal salePrice = toDecimal(deal.salePrice());
        BigDecimal normalPrice = toDecimal(deal.normalPrice());
        return salePrice.compareTo(normalPrice) < 0;
    }

    private CheapSharkDealDto pickBetterDeal(CheapSharkDealDto left, CheapSharkDealDto right) {
        int priceCompare = toDecimal(left.salePrice()).compareTo(toDecimal(right.salePrice()));
        if (priceCompare != 0) {
            return priceCompare < 0 ? left : right;
        }

        int ratingCompare = toDecimal(left.dealRating()).compareTo(toDecimal(right.dealRating()));
        if (ratingCompare != 0) {
            return ratingCompare > 0 ? left : right;
        }

        return safeValue(left.title()).compareToIgnoreCase(safeValue(right.title())) <= 0 ? left : right;
    }

    private boolean isTruthy(String value) {
        if (value == null) {
            return false;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("1") || normalized.equals("true") || normalized.equals("yes");
    }

    private BigDecimal toDecimal(String value) {
        try {
            if (value == null || value.isBlank()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(value.trim());
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeValue(String value) {
        return Objects.toString(value, "-");
    }

    public void sendTestWishlistSaleEmail(String recipientEmail) {
        CheapSharkDealDto testDeal = new CheapSharkDealDto(
                "DMC5_INTERNAL",
                "Devil May Cry 5",
                null,
                "dmc5-deal-test",
                "1",
                "12345",
                "14.99",
                "29.99",
                "1",
                "50.0",
                "87",
                "Very Positive",
                "87",
                "5000",
                null,
                1L,
                1L,
                "8.5",
                "thumb-dmc5"
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject("Twoje gry z listy życzeń są teraz na promocji");
        message.setText(buildTestMessageText(testDeal));
        sendEmailSafely(message, recipientEmail);
    }

    public TestEmailContent buildTestEmailContent(String recipientEmail) {
        CheapSharkDealDto testDeal = new CheapSharkDealDto(
                "DMC5_INTERNAL",
                "Devil May Cry 5",
                null,
                "dmc5-deal-test",
                "1",
                "12345",
                "14.99",
                "29.99",
                "1",
                "50.0",
                "87",
                "Very Positive",
                "87",
                "5000",
                null,
                1L,
                1L,
                "8.5",
                "thumb-dmc5"
        );

        return new TestEmailContent(
                recipientEmail,
                fromAddress,
                "Twoje gry z listy życzeń są teraz na promocji",
                buildTestMessageText(testDeal)
        );
    }

    private String buildTestMessageText(CheapSharkDealDto deal) {
        StringBuilder builder = new StringBuilder();
        builder.append("Cześć,\n\n");
        builder.append("To jest testowe powiadomienie o promocji. Wykryliśmy nowe promocje dla gier z Twojej listy życzeń:\n\n");
        builder.append("- ")
                .append(safeValue(deal.title()))
                .append(" | cena promocyjna: $").append(safeValue(deal.salePrice()))
                .append(" | cena regularna: $").append(safeValue(deal.normalPrice()))
                .append(" | oszczędzasz: ").append(safeValue(deal.savings())).append("%")
                .append(" | link: ").append(cheapSharkService.buildRedirectUrl(deal.dealId()))
                .append("\n");
        builder.append("\nJeśli chcesz zmienić preferencje powiadomień, zrób to w swoim profilu.");
        return builder.toString();
    }

    private record SaleAlert(String gameId, CheapSharkDealDto deal) {
    }

     public static record TestEmailContent(String to, String from, String subject, String body) {
     }
 }
