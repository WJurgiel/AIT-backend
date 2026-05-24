package com.ait.aitbackend.notification.service;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.ait.aitbackend.games.dto.rawg.RawgGamesResponseDto;
import com.ait.aitbackend.games.service.CheapSharkService;
import com.ait.aitbackend.games.service.RawgService;
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
import java.util.HashMap;
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
    private static final String TEST_USERNAME = "Wojtek123";
    private static final String TEST_GAME_TITLE = "Devil May Cry 5";
    private static final String TEST_GAME_SLUG = "devil-may-cry-5";

    private final UserProfileRepository userProfileRepository;
    private final CheapSharkService cheapSharkService;
    private final RawgService rawgService;
    private final JavaMailSender mailSender;
    private final WishlistSaleNotificationHistoryRepository historyRepository;
    private final String fromAddress;

    public WishlistSaleNotificationService(
            UserProfileRepository userProfileRepository,
            CheapSharkService cheapSharkService,
            RawgService rawgService,
            JavaMailSender mailSender,
            WishlistSaleNotificationHistoryRepository historyRepository,
            @Value("${notifications.wishlist-on-sale.from:no-reply@ait.local}") String fromAddress
    ) {
        this.userProfileRepository = userProfileRepository;
        this.cheapSharkService = cheapSharkService;
        this.rawgService = rawgService;
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
                        deal -> normalizeToInternalGameId(deal.gameId()),
                        Function.identity(),
                        this::pickBetterDeal,
                        LinkedHashMap::new
                ));

        Map<Integer, RawgGamesResponseDto.RawgGameDto> rawgGameCache = new HashMap<>();
        int sentEmails = 0;
        for (UserProfile user : userProfileRepository.findAll()) {
            if (!shouldNotify(user)) {
                continue;
            }

            List<SaleAlert> alerts = buildAlertsForUser(user, bestDealsByGameId, rawgGameCache);
            if (alerts.isEmpty()) {
                continue;
            }

            sendEmail(user, alerts);
            historyRepository.saveAll(alerts.stream()
                    .map(alert -> new WishlistSaleNotificationHistory(
                            user.getId(),
                            alert.internalGameId(),
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

    private List<SaleAlert> buildAlertsForUser(
            UserProfile user,
            Map<String, CheapSharkDealDto> bestDealsByGameId,
            Map<Integer, RawgGamesResponseDto.RawgGameDto> rawgGameCache
    ) {
        Set<String> favoriteGameIds = normalizeFavorites(user);
        if (favoriteGameIds.isEmpty()) {
            return List.of();
        }

        List<SaleAlert> alerts = new ArrayList<>();
        for (String favoriteGameId : favoriteGameIds) {
            ResolvedFavoriteGame resolvedFavoriteGame = resolveFavoriteGame(favoriteGameId, rawgGameCache);
            if (resolvedFavoriteGame == null) {
                continue;
            }

            CheapSharkDealDto deal = bestDealsByGameId.get(resolvedFavoriteGame.internalGameId());
            if (deal == null) {
                continue;
            }

            if (historyRepository.existsByUserIdAndGameIdAndDealIdAndSalePrice(
                    user.getId(),
                    resolvedFavoriteGame.internalGameId(),
                    normalize(deal.dealId()),
                    deal.salePrice()
            )) {
                continue;
            }

            alerts.add(new SaleAlert(resolvedFavoriteGame.internalGameId(), resolvedFavoriteGame.displayName(), deal));
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
        message.setText(buildMessageText(user.getUsername(), alerts));
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

    private String buildMessageText(String recipientName, List<SaleAlert> alerts) {
        StringBuilder builder = new StringBuilder();
        builder.append("Cześć ").append(safeValue(recipientName)).append(",\n\n");
        builder.append("Wykryliśmy nowe promocje dla gier z Twojej listy życzeń:\n\n");

        for (SaleAlert alert : alerts) {
            CheapSharkDealDto deal = alert.deal();
            builder.append("- ")
                    .append(safeValue(alert.displayName()))
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

    @SuppressWarnings("unused")
    public void sendTestWishlistSaleEmail(String recipientEmail) {
        CheapSharkDealDto testDeal = new CheapSharkDealDto(
                "DMC5_INTERNAL",
                TEST_GAME_TITLE,
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
        message.setText(buildMessageText(TEST_USERNAME, List.of(new SaleAlert(TEST_GAME_SLUG, TEST_GAME_TITLE, testDeal))));
        sendEmailSafely(message, recipientEmail);
    }

    @SuppressWarnings("unused")
    public TestEmailContent buildTestEmailContent(String recipientEmail) {
        CheapSharkDealDto testDeal = new CheapSharkDealDto(
                "DMC5_INTERNAL",
                TEST_GAME_TITLE,
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
                buildMessageText(TEST_USERNAME, List.of(new SaleAlert(TEST_GAME_SLUG, TEST_GAME_TITLE, testDeal)))
        );
    }

    private ResolvedFavoriteGame resolveFavoriteGame(String favoriteGameId, Map<Integer, RawgGamesResponseDto.RawgGameDto> rawgGameCache) {
        String normalizedFavoriteId = normalize(favoriteGameId);
        if (normalizedFavoriteId.isBlank()) {
            return null;
        }

        Integer rawgId = tryParseInteger(normalizedFavoriteId);
        if (rawgId == null) {
            String internalGameId = normalizeToInternalGameId(normalizedFavoriteId);
            return internalGameId.isBlank() ? null : new ResolvedFavoriteGame(internalGameId, normalizedFavoriteId);
        }

        RawgGamesResponseDto.RawgGameDto rawgGame = getRawgGame(rawgId, rawgGameCache);
        if (rawgGame == null) {
            return null;
        }

        String displayName = firstNonBlank(rawgGame.getName(), rawgGame.getSlug(), normalizedFavoriteId);
        String internalGameId = normalizeToInternalGameId(firstNonBlank(rawgGame.getSlug(), rawgGame.getName(), normalizedFavoriteId));
        if (internalGameId.isBlank()) {
            return null;
        }

        return new ResolvedFavoriteGame(internalGameId, displayName);
    }

    private RawgGamesResponseDto.RawgGameDto getRawgGame(Integer rawgGameId, Map<Integer, RawgGamesResponseDto.RawgGameDto> rawgGameCache) {
        if (rawgGameId == null) {
            return null;
        }

        if (rawgGameCache.containsKey(rawgGameId)) {
            return rawgGameCache.get(rawgGameId);
        }

        try {
            RawgGamesResponseDto.RawgGameDto rawgGame = rawgService.getGameById(rawgGameId);
            rawgGameCache.put(rawgGameId, rawgGame);
            return rawgGame;
        } catch (Exception ex) {
            log.warn("Failed to resolve RAWG game {} for wishlist notification: {}", rawgGameId, ex.getMessage());
            rawgGameCache.put(rawgGameId, null);
            return null;
        }
    }

    private Integer tryParseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private String normalizeToInternalGameId(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return "";
        }

        return normalized.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private record SaleAlert(String internalGameId, String displayName, CheapSharkDealDto deal) {
    }

    private record ResolvedFavoriteGame(String internalGameId, String displayName) {
    }

    public record TestEmailContent(String to, String from, String subject, String body) {
    }
}

