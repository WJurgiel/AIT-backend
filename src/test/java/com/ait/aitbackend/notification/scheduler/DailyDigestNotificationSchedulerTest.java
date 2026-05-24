package com.ait.aitbackend.notification.scheduler;

import com.ait.aitbackend.notification.service.DailyDigestNotificationService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DailyDigestNotificationSchedulerTest {

    @Test
    void shouldDelegateToNotificationService() {
        DailyDigestNotificationService service = mock(DailyDigestNotificationService.class);
        DailyDigestNotificationScheduler scheduler = new DailyDigestNotificationScheduler(service);

        scheduler.sendDailyDigestNotificationsScheduled();

        verify(service).sendDailyDigestNotifications();
    }
}

