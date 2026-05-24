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

    private boolean shouldNotify(UserProfile user) {
        return user != null
                && !normalizeEmail(user.getEmail()).isBlank()
                && user.getPreferences() != null
                && user.getPreferences().isDailyDigest();
    }

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

    private boolean sendEmail(UserProfile user, List<CheapSharkDealDto> topDeals) {
        String recipientEmail = normalizeEmail(user.getEmail());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject(DEFAULT_SUBJECT);
        message.setText(buildMessageText(user.getUsername(), topDeals));
        return sendEmailSafely(message, recipientEmail);
    }

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

    private String normalizeToInternalGameId(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return "";
        }

        return normalized.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeEmail(String email) {
        return normalize(email);
    }

    private String safeValue(String value) {
        return Objects.toString(value, "-");
    }
}



