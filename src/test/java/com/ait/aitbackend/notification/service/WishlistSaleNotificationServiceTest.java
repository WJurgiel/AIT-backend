package com.ait.aitbackend.notification.service;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.ait.aitbackend.games.service.CheapSharkService;
import com.ait.aitbackend.games.service.RawgService;
import com.ait.aitbackend.games.dto.rawg.RawgGamesResponseDto;
import com.ait.aitbackend.notification.entity.WishlistSaleNotificationHistory;
import com.ait.aitbackend.notification.repository.WishlistSaleNotificationHistoryRepository;
import com.ait.aitbackend.user.entity.UserPreferences;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WishlistSaleNotificationServiceTest {

    /**
     * Weryfikuje, czy użytkownik otrzymuje pojedynczy, zbiorczy e-mail ze zniżkami na jego wymarzone gry oraz czy te powiadomienia zapisują się w historii.
     */
    @Test
    void shouldSendAggregatedEmailForNewWishlistSale() {
        UserProfileRepository userRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
        CheapSharkService cheapSharkService = org.mockito.Mockito.mock(CheapSharkService.class);
        RawgService rawgService = org.mockito.Mockito.mock(RawgService.class);
        JavaMailSender mailSender = org.mockito.Mockito.mock(JavaMailSender.class);
        WishlistSaleNotificationHistoryRepository historyRepository = org.mockito.Mockito.mock(WishlistSaleNotificationHistoryRepository.class);

        UserProfile user = new UserProfile("player-one", "player-one@mail.com", "secret");
        user.setId(1L);
        UserPreferences preferences = user.getPreferences();
        preferences.setWishlistOnSale(true);
        preferences.setFavoriteGameIdsList(List.of("101", "202", "404"));

        UserProfile mutedUser = new UserProfile("player-two", "player-two@mail.com", "secret");
        mutedUser.setId(2L);
        mutedUser.getPreferences().setWishlistOnSale(false);
        mutedUser.getPreferences().setFavoriteGameIdsList(List.of("101"));

        when(userRepository.findAll()).thenReturn(List.of(user, mutedUser));
        when(rawgService.getGameById(101)).thenReturn(rawgGame(101, "Game Alpha", "game-alpha"));
        when(rawgService.getGameById(202)).thenReturn(rawgGame(202, "Game Beta", "game-beta"));
        when(rawgService.getGameById(404)).thenReturn(rawgGame(404, "Not On Sale", "not-on-sale"));
        when(cheapSharkService.getDeals(null)).thenReturn(List.of(
                new CheapSharkDealDto("GAMEALPHA_INTERNAL_A", "Game Alpha - Store A", null, "deal-a", "1", "GAMEALPHA", "3.99", "19.99", "1", "80.0", "88", "Positive", "92", "1200", null, 1L, 1L, "7.0", "thumb-a"),
                new CheapSharkDealDto("GAMEALPHA_INTERNAL_B", "Game Alpha - Store B", null, "deal-b", "7", "GAMEALPHA", "2.99", "19.99", "1", "85.0", "88", "Positive", "92", "1200", null, 1L, 1L, "8.0", "thumb-b"),
                new CheapSharkDealDto("GAMEBETA_INTERNAL", "Game Beta", null, "deal-c", "25", "GAMEBETA", "9.99", "19.99", "true", "50.0", "81", "Very Positive", "90", "900", null, 1L, 1L, "9.0", "thumb-c"),
                new CheapSharkDealDto("NOTONSALE_INTERNAL", "Not On Sale", null, "deal-d", "25", "NOTONSALE", "10.00", "10.00", "0", "0.0", "0", "N/A", "0", "0", null, 1L, 1L, "0.0", "thumb-d")
        ));
        when(cheapSharkService.buildRedirectUrl(anyString())).thenAnswer(invocation ->
                "https://www.cheapshark.com/redirect?dealID=" + invocation.getArgument(0));
        when(historyRepository.existsByUserIdAndGameIdAndDealIdAndSalePrice(anyLong(), anyString(), anyString(), anyString())).thenReturn(false);

        WishlistSaleNotificationService service = new WishlistSaleNotificationService(
                userRepository,
                cheapSharkService,
                rawgService,
                mailSender,
                historyRepository,
                "alerts@ait.local"
        );

        int sentEmails = service.sendWishlistSaleNotifications();

        assertEquals(1, sentEmails);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        String[] recipients = message.getTo();
        assertNotNull(recipients);
        assertNotNull(message.getText());
        assertEquals("alerts@ait.local", message.getFrom());
        assertEquals("player-one@mail.com", recipients[0]);
        assertEquals("Twoje gry z listy życzeń są teraz na promocji", message.getSubject());
        assertTrue(message.getText().contains("Cześć player-one"));
        assertTrue(message.getText().contains("Game Alpha"));
        assertTrue(message.getText().contains("Game Beta"));
        assertTrue(message.getText().contains("https://www.cheapshark.com/redirect?dealID=deal-b"));
        assertTrue(message.getText().contains("https://www.cheapshark.com/redirect?dealID=deal-c"));
        assertFalse(message.getText().contains("Game Alpha - Store A"));

        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<Iterable<WishlistSaleNotificationHistory>> historiesCaptor = (ArgumentCaptor) ArgumentCaptor.forClass(List.class);
        verify(historyRepository).saveAll(historiesCaptor.capture());
        List<WishlistSaleNotificationHistory> histories = StreamSupport.stream(historiesCaptor.getValue().spliterator(), false).toList();
        assertEquals(2, histories.size());
        assertTrue(histories.stream().anyMatch(history ->
                Objects.equals(history.getUserId(), 1L)
                        && Objects.equals(history.getGameId(), "GAMEALPHA")
                        && Objects.equals(history.getDealId(), "deal-b")
                        && Objects.equals(history.getSalePrice(), "2.99")));
        assertTrue(histories.stream().anyMatch(history ->
                Objects.equals(history.getUserId(), 1L)
                        && Objects.equals(history.getGameId(), "GAMEBETA")
                        && Objects.equals(history.getDealId(), "deal-c")
                        && Objects.equals(history.getSalePrice(), "9.99")));
    }

    /**
     * Sprawdza przypadek brzegowy – jeśli dana promocja na grę została już wcześniej wysłana użytkownikowi, serwis blokuje powtórną wysyłkę na podstawie historii.
     */
    @Test
    void shouldSkipAlreadyNotifiedSale() {
        UserProfileRepository userRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
        CheapSharkService cheapSharkService = org.mockito.Mockito.mock(CheapSharkService.class);
        RawgService rawgService = org.mockito.Mockito.mock(RawgService.class);
        JavaMailSender mailSender = org.mockito.Mockito.mock(JavaMailSender.class);
        WishlistSaleNotificationHistoryRepository historyRepository = org.mockito.Mockito.mock(WishlistSaleNotificationHistoryRepository.class);

        UserProfile user = new UserProfile("player-one", "player-one@mail.com", "secret");
        user.setId(1L);
        user.getPreferences().setWishlistOnSale(true);
        user.getPreferences().setFavoriteGameIdsList(List.of("101"));

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(rawgService.getGameById(101)).thenReturn(rawgGame(101, "Game Alpha", "game-alpha"));
        when(cheapSharkService.getDeals(null)).thenReturn(List.of(
                new CheapSharkDealDto("GAMEALPHA_INTERNAL", "Game Alpha", null, "deal-a", "1", "GAMEALPHA", "3.99", "19.99", "1", "80.0", "88", "Positive", "92", "1200", null, 1L, 1L, "7.0", "thumb-a")
        ));
        when(cheapSharkService.buildRedirectUrl(anyString())).thenAnswer(invocation ->
                "https://www.cheapshark.com/redirect?dealID=" + invocation.getArgument(0));
        when(historyRepository.existsByUserIdAndGameIdAndDealIdAndSalePrice(anyLong(), anyString(), anyString(), anyString())).thenReturn(true);

        WishlistSaleNotificationService service = new WishlistSaleNotificationService(
                userRepository,
                cheapSharkService,
                rawgService,
                mailSender,
                historyRepository,
                "alerts@ait.local"
        );

        int sentEmails = service.sendWishlistSaleNotifications();

        assertEquals(0, sentEmails);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(historyRepository, never()).saveAll(any());
    }

    /**
     * Testuje działanie metody budującej testowy podgląd e-maila. Upewnia się, że jej output poprawnie wykorzystuje systemowy szablon i przykładowe dane.
     */
    @Test
    void shouldBuildTestEmailContentUsingSharedTemplate() {
        WishlistSaleNotificationService service = new WishlistSaleNotificationService(
                org.mockito.Mockito.mock(UserProfileRepository.class),
                org.mockito.Mockito.mock(CheapSharkService.class),
                org.mockito.Mockito.mock(RawgService.class),
                org.mockito.Mockito.mock(JavaMailSender.class),
                org.mockito.Mockito.mock(WishlistSaleNotificationHistoryRepository.class),
                "alerts@ait.local"
        );

        WishlistSaleNotificationService.TestEmailContent content = service.buildTestEmailContent("trap@mail.com");

        assertEquals("trap@mail.com", content.to());
        assertEquals("alerts@ait.local", content.from());
        assertTrue(content.body().contains("Cześć Wojtek123"));
        assertTrue(content.body().contains("Devil May Cry 5"));
    }

    private static RawgGamesResponseDto.RawgGameDto rawgGame(int id, String name, String slug) {
        RawgGamesResponseDto.RawgGameDto rawgGame = new RawgGamesResponseDto.RawgGameDto();
        rawgGame.setId(id);
        rawgGame.setName(name);
        rawgGame.setSlug(slug);
        return rawgGame;
    }
}







