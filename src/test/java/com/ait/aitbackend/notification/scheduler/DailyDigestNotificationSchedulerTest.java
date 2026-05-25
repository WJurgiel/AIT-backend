package com.ait.aitbackend.notification.scheduler;

import com.ait.aitbackend.notification.service.DailyDigestNotificationService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DailyDigestNotificationSchedulerTest {

    /**
     * Sprawdza, czy scheduler dla dziennych podsumowań (Daily Digest) poprawnie deleguje cykliczne zadanie, wywołując metodę wysyłającą w serwisie.
     */
    @Test
    void shouldDelegateToNotificationService() {
        DailyDigestNotificationService service = mock(DailyDigestNotificationService.class);
        DailyDigestNotificationScheduler scheduler = new DailyDigestNotificationScheduler(service);

        scheduler.sendDailyDigestNotificationsScheduled();

        verify(service).sendDailyDigestNotifications();
    }
}

