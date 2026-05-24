package com.ait.aitbackend.notification.service;

import com.ait.aitbackend.games.dto.cheapshark.CheapSharkDealDto;
import com.ait.aitbackend.games.service.CheapSharkService;
import com.ait.aitbackend.user.entity.UserPreferences;
import com.ait.aitbackend.user.entity.UserProfile;
import com.ait.aitbackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DailyDigestNotificationServiceTest {

    @Test
    void shouldSendTopFiveDealsOnlyToOptInUsers() {
        UserProfileRepository userRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
        CheapSharkService cheapSharkService = org.mockito.Mockito.mock(CheapSharkService.class);
        JavaMailSender mailSender = org.mockito.Mockito.mock(JavaMailSender.class);

        UserProfile optedInUser = new UserProfile("digest-user", "digest-user@mail.com", "secret");
        optedInUser.setId(1L);
        UserPreferences optedInPreferences = optedInUser.getPreferences();
        optedInPreferences.setDailyDigest(true);

        UserProfile optedOutUser = new UserProfile("muted-user", "muted-user@mail.com", "secret");
        optedOutUser.setId(2L);
        optedOutUser.getPreferences().setDailyDigest(false);

        when(userRepository.findAll()).thenReturn(List.of(optedInUser, optedOutUser));
        when(cheapSharkService.getDeals(null)).thenReturn(List.of(
                deal("ONE_A", "Game One - Store A", "deal-1a", "1", "game-one", "1.00", "10.00", "1", "90.0", "95.0"),
                deal("ONE_B", "Game One - Store B", "deal-1b", "2", "game-one", "0.80", "10.00", "1", "92.0", "96.0"),
                deal("TWO", "Game Two", "deal-2", "3", "game-two", "2.00", "12.00", "true", "83.3", "93.0"),
                deal("THREE", "Game Three", "deal-3", "4", "game-three", "3.00", "15.00", "1", "80.0", "91.0"),
                deal("FOUR", "Game Four", "deal-4", "5", "game-four", "4.00", "20.00", "1", "75.0", "89.0"),
                deal("FIVE", "Game Five", "deal-5", "6", "game-five", "5.00", "25.00", "1", "70.0", "87.0"),
                deal("SIX", "Game Six", "deal-6", "7", "game-six", "6.00", "30.00", "1", "65.0", "86.0")
        ));
        when(cheapSharkService.buildRedirectUrl(anyString())).thenAnswer(invocation ->
                "https://www.cheapshark.com/redirect?dealID=" + invocation.getArgument(0));

        DailyDigestNotificationService service = new DailyDigestNotificationService(
                userRepository,
                cheapSharkService,
                mailSender,
                "alerts@ait.local"
        );

        int sentEmails = service.sendDailyDigestNotifications();

        assertEquals(1, sentEmails);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getTo());
        assertEquals("alerts@ait.local", message.getFrom());
        assertEquals("digest-user@mail.com", message.getTo()[0]);
        assertEquals("Twój daily digest: top 5 najlepszych deali dnia", message.getSubject());

        String body = message.getText();
        assertNotNull(body);
        assertTrue(body.contains("Cześć digest-user"));
        assertTrue(body.contains("1. Game One - Store B"));
        assertTrue(body.contains("2. Game Two"));
        assertTrue(body.contains("3. Game Three"));
        assertTrue(body.contains("4. Game Four"));
        assertTrue(body.contains("5. Game Five"));
        assertTrue(body.contains("https://www.cheapshark.com/redirect?dealID=deal-1b"));
        assertTrue(body.contains("https://www.cheapshark.com/redirect?dealID=deal-2"));
        assertTrue(body.contains("https://www.cheapshark.com/redirect?dealID=deal-5"));
        assertTrue(body.contains("Wiadomość została wygenerowana automatycznie."));
        org.junit.jupiter.api.Assertions.assertFalse(body.contains("Game Six"));
    }

    @Test
    void shouldSendSeparateDigestEmailsToEachOptInUser() {
        UserProfileRepository userRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
        CheapSharkService cheapSharkService = org.mockito.Mockito.mock(CheapSharkService.class);
        JavaMailSender mailSender = org.mockito.Mockito.mock(JavaMailSender.class);

        UserProfile firstUser = new UserProfile("digest-user-1", "digest-user-1@mail.com", "secret");
        firstUser.setId(1L);
        firstUser.getPreferences().setDailyDigest(true);

        UserProfile secondUser = new UserProfile("digest-user-2", "digest-user-2@mail.com", "secret");
        secondUser.setId(2L);
        secondUser.getPreferences().setDailyDigest(true);

        when(userRepository.findAll()).thenReturn(List.of(firstUser, secondUser));
        when(cheapSharkService.getDeals(null)).thenReturn(List.of(
                deal("ONE_A", "Game One", "deal-1", "1", "game-one", "1.00", "10.00", "1", "90.0", "95.0")
        ));
        when(cheapSharkService.buildRedirectUrl(anyString())).thenAnswer(invocation ->
                "https://www.cheapshark.com/redirect?dealID=" + invocation.getArgument(0));

        DailyDigestNotificationService service = new DailyDigestNotificationService(
                userRepository,
                cheapSharkService,
                mailSender,
                "alerts@ait.local"
        );

        int sentEmails = service.sendDailyDigestNotifications();

        assertEquals(2, sentEmails);
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());
        List<SimpleMailMessage> messages = messageCaptor.getAllValues();
        assertEquals(2, messages.size());
        assertEquals("digest-user-1@mail.com", messages.get(0).getTo()[0]);
        assertEquals("digest-user-2@mail.com", messages.get(1).getTo()[0]);
    }

    private static CheapSharkDealDto deal(
            String internalName,
            String title,
            String dealId,
            String storeId,
            String gameId,
            String salePrice,
            String normalPrice,
            String isOnSale,
            String savings,
            String dealRating
    ) {
        return new CheapSharkDealDto(
                internalName,
                title,
                null,
                dealId,
                storeId,
                gameId,
                salePrice,
                normalPrice,
                isOnSale,
                savings,
                "90",
                "Very Positive",
                "95",
                "1000",
                null,
                1L,
                1L,
                dealRating,
                "thumb"
        );
    }
}




