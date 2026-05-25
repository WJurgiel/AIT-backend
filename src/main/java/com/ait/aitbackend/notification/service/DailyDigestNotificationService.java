package com.ait.aitbackend.notification.service;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.ait.aitbackend.games.service.CheapSharkService;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DailyDigestNotificationService {
    private static final String DEFAULT_SUBJECT = "Twój daily digest: top 5 najlepszych deali dnia";

    private final UserProfileRepository userProfileRepository;
    private final CheapSharkService cheapSharkService;
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public DailyDigestNotificationService(
            UserProfileRepository userProfileRepository,
            CheapSharkService cheapSharkService,
            JavaMailSender mailSender,
            @Value("${notifications.daily-digest.from:no-reply@ait.local}") String fromAddress
    ) {
        this.userProfileRepository = userProfileRepository;
        this.cheapSharkService = cheapSharkService;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    /**
     * Główna metoda agregująca. Kompiluje najlepsze oferty, iteruje po profilach użytkowników z aktywną subskrypcją i wysyła im dzienne podsumowania.
     */
    public int sendDailyDigestNotifications() {
        List<CheapSharkDealDto> topDeals = buildTopDeals();

        int sentEmails = 0;
        
        // Fetch all users into a list first to avoid issues with lazy loading and transaction rollback
        List<UserProfile> allUsers = userProfileRepository.findAll();
        
        for (UserProfile user : allUsers) {
            if (!shouldNotify(user)) {
                continue;
            }

            try {
                if (sendEmail(user, topDeals)) {
                    sentEmails++;
                }
            } catch (Exception ex) {
                log.error("Failed to send daily digest notification to user {} ({}). Error: {}", 
                        user.getId(), user.getEmail(), ex.getMessage(), ex);
            }
        }

        return sentEmails;
    }

    /**
     * Waliduje profil, upewniając się, że użytkownik w ogóle posiada ustawiony adres e-mail oraz czy ma aktywną zgodę na daily digest w swoich preferencjach.
     * @param user
     */
    private boolean shouldNotify(UserProfile user) {
        return user != null
                && !normalizeEmail(user.getEmail()).isBlank()
                && user.getPreferences() != null
                && user.getPreferences().isDailyDigest();
    }

    /**
     * Odpytuje zewnętrzne API o promocje, filtruje je, rozwiązuje duplikaty na podstawie wewnętrznego ID gry i zwraca top 5 posortowanych m.in. po procencie zniżki i ocenie.
     * @return
     */
    private List<CheapSharkDealDto> buildTopDeals() {
        Map<String, CheapSharkDealDto> bestDealsByGameId = cheapSharkService.getDeals(null).stream()
                .filter(this::isOnSale)
                .filter(deal -> deal.gameId() != null && !deal.gameId().isBlank())
                .collect(Collectors.toMap(
                        deal -> normalizeToInternalGameId(deal.gameId()),
                        Function.identity(),
                        this::pickBetterDeal,
                        LinkedHashMap::new
                ));

        return bestDealsByGameId.values().stream()
                .sorted(Comparator
                        .comparing((CheapSharkDealDto deal) -> toDecimal(deal.savings()), Comparator.reverseOrder())
                        .thenComparing(deal -> toDecimal(deal.dealRating()), Comparator.reverseOrder())
                        .thenComparing(deal -> toDecimal(deal.salePrice()))
                        .thenComparing(deal -> safeValue(deal.title())))
                .limit(5)
                .toList();
    }

    /**
     * Przygotowuje obiekt e-maila SimpleMailMessage z odpowiednim nadawcą, tematem i wygenerowaną treścią, po czym przekazuje go do bezpiecznej wysyłki.
     * @param user
     * @param topDeals
     */
    private boolean sendEmail(UserProfile user, List<CheapSharkDealDto> topDeals) {
        String recipientEmail = normalizeEmail(user.getEmail());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject(DEFAULT_SUBJECT);
        message.setText(buildMessageText(user.getUsername(), topDeals));
        return sendEmailSafely(message, recipientEmail);
    }

    /**
     * Odpowiada za bezpośrednie wysłanie wiadomości poprzez JavaMailSender. Posiada mechanizm pojedynczej ponownej próby w przypadku wyjątku MailException i rzetelnie loguje błędy.
     * @param message
     * @param recipientEmail
     */
    private boolean sendEmailSafely(SimpleMailMessage message, String recipientEmail) {
        try {
            mailSender.send(message);
            log.info("Daily digest email sent to {}", recipientEmail);
            return true;
        } catch (MailException ex) {
            log.warn("Failed to send daily digest email to {}. Retrying once. Error: {}", recipientEmail, ex.getMessage());
            try {
                mailSender.send(new SimpleMailMessage(message));
                log.info("Daily digest email sent to {} on retry", recipientEmail);
                return true;
            } catch (Exception retryEx) {
                log.warn("Retry failed for daily digest email to {}. Message will be logged. Error: {}", recipientEmail, retryEx.getMessage());
                log.debug("Daily digest email content that would have been sent:", retryEx);
                log.info("EMAIL_LOG: To={}, Subject={}, Body preview={}",
                        recipientEmail,
                        message.getSubject(),
                        message.getText() != null && message.getText().length() > 100
                                ? message.getText().substring(0, 100) + "..."
                                : message.getText());
                return false;
            }
        } catch (Exception ex) {
            log.warn("Failed to send daily digest email to {}. Message will be logged. Error: {}", recipientEmail, ex.getMessage());
            log.debug("Daily digest email content that would have been sent:", ex);
            log.info("EMAIL_LOG: To={}, Subject={}, Body preview={}",
                    recipientEmail,
                    message.getSubject(),
                    message.getText() != null && message.getText().length() > 100
                            ? message.getText().substring(0, 100) + "..."
                            : message.getText());
            return false;
        }
    }

    /**
     * Buduje blok tekstowy e-maila, witając użytkownika po imieniu i dynamicznie listując wszystkie 5 ofert wraz z ich cenami i reflinkami. Obsługuje też przypadek braku przecen.
     * @param recipientName
     * @param deals
     */
    private String buildMessageText(String recipientName, List<CheapSharkDealDto> deals) {
        StringBuilder builder = new StringBuilder();
        builder.append("Cześć ").append(safeValue(recipientName)).append(",\n\n");
        builder.append("Oto top 5 najlepszych deali dnia:\n\n");

        if (deals.isEmpty()) {
            builder.append("Dzisiaj nie znaleźliśmy żadnych promocji.\n");
        } else {
            for (int i = 0; i < deals.size(); i++) {
                CheapSharkDealDto deal = deals.get(i);
                builder.append(i + 1).append(". ")
                        .append(safeValue(deal.title()))
                        .append(" | cena promocyjna: $").append(safeValue(deal.salePrice()))
                        .append(" | cena regularna: $").append(safeValue(deal.normalPrice()))
                        .append(" | oszczędzasz: ").append(safeValue(deal.savings())).append("%")
                        .append(" | link: ").append(cheapSharkService.buildRedirectUrl(deal.dealId()))
                        .append("\n");
            }
        }

        builder.append("\nWiadomość została wygenerowana automatycznie.");
        return builder.toString();
    }

    /**
     * Waliduje, czy oferta z API faktycznie kwalifikuje się jako przeceniona. Sprawdza dedykowaną flagę zwrotną lub porównuje matematycznie cenę zniżkową z normalną.
     * @param deal
     */
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

    /**
     * Rozstrzyga konflikt, gdy ta sama gra posiada wiele promocji w różnych sklepach. Preferuje wariant z najniższą ceną, wyższą oceną lub po prostu sortuje alfabetycznie tytulem.
     * @param left
     * @param right
     */
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

    /**
     * Prosty ewaluator prawdy dla ciągów tekstowych. Uznaje warianty typu "1", "true", lub "yes" za logiczną flagę true.
     * @param value
     */
    private boolean isTruthy(String value) {
        if (value == null) {
            return false;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("1") || normalized.equals("true") || normalized.equals("yes");
    }

    /**
     * Bezpiecznie konwertuje tekstową wartość liczbową do znormalizowanego obiektu BigDecimal, cicho połykając wyjątki i zwracając w ich miejsce 0
     * @param value
     */
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

    /**
     * Konstruuje surowy identyfikator, usuwając ze stringa wszelkie znaki inne niż litery i cyfry, a całość transformując na wielkie litery.
     * @param value
     */
    private String normalizeToInternalGameId(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return "";
        }

        return normalized.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    /**
     * Standardowa metoda pomocnicza chroniąca przed NullPointerException. Jeśli wartość nie jest nullem, zwraca jej odpowiednik po usunięciu białych znaków (trim).
     * @param value
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Szybki alias mapujący czyszczenie adresu e-mail na istniejącą już metodę normalize.
     * @param email
     */
    private String normalizeEmail(String email) {
        return normalize(email);
    }

    /**
     * Chroni system powiadomień przed słowem "null" w tekście – jeśli wartość nie istnieje, zostaje bezpiecznie zastąpiona myślnikiem.
     * @param value
     */
    private String safeValue(String value) {
        return Objects.toString(value, "-");
    }
}



