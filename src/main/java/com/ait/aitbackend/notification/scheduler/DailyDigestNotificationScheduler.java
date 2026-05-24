package com.ait.aitbackend.notification.scheduler;

import com.ait.aitbackend.notification.service.DailyDigestNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyDigestNotificationScheduler {
    private final DailyDigestNotificationService dailyDigestNotificationService;

    @Scheduled(
            fixedDelayString = "${notifications.daily-digest.scheduler-delay-ms:86400000}",
            initialDelayString = "${notifications.daily-digest.scheduler-delay-ms:86400000}"
    )
    public void sendDailyDigestNotificationsScheduled() {
        int sentEmails = dailyDigestNotificationService.sendDailyDigestNotifications();
        log.info("Daily digest notification run finished; sent {} email(s)", sentEmails);
    }
}
