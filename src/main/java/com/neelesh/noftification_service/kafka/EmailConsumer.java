package com.neelesh.noftification_service.kafka;

import com.neelesh.noftification_service.dto.NotificationEvent;
import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.model.UserPreferences;
import com.neelesh.noftification_service.provider.EmailSender;
import com.neelesh.noftification_service.repository.NotificationRepository;
import com.neelesh.noftification_service.service.DndSchedulerService;
import com.neelesh.noftification_service.service.DndService;
import com.neelesh.noftification_service.service.MetricService;
import com.neelesh.noftification_service.service.UserPreferencesService;
import com.sendgrid.helpers.mail.objects.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final NotificationRepository notificationRepository;
    private final UserPreferencesService userPreferencesService;
    private final EmailSender emailSender;
    private final DndService dndService;
    private final DndSchedulerService dndSchedulerService;
    private final MetricService metricService;

    @KafkaListener(
            topics = "${app.kafka.topics.email}",
            groupId = "notification-group"
    )
    public void  consume(NotificationEvent event, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        long startTime = System.currentTimeMillis();
        notificationRepository.findById(event.getNotificationId())
                .ifPresent(notification -> {
                    if (notification.getStatus() == Notification.Status.DELIVERED) {
                        log.info("Already delivered, skipping");
                        return;
                    }
                    try {
                        UserPreferences userPreferences = userPreferencesService.getUserPreferences(notification.getUserId());
                        if (dndService.isDndActive(userPreferences)) {
                            log.info("[EMAIL] User in DND, delaying notificationId={}",
                                    event.getNotificationId());
                            notification.setStatus(Notification.Status.DELAYED);
                            notificationRepository.save(notification);
                            dndSchedulerService.scheduleRetry(notification, userPreferences.getDndEnd(), userPreferences.getDndStart(), userPreferences.getTimeZone());
                            metricService.recordDelivery(notification.getChannel(), Notification.Status.DELAYED.name());
                            return;
                        }
                        emailSender.send(notification, userPreferences);

                        log.info("[EMAIL] Send notificationId={} to userId={}",
                                event.getNotificationId(), event.getUserId());
                        notification.setStatus(Notification.Status.DELIVERED);
                        metricService.recordDelivery(notification.getChannel(), Notification.Status.DELIVERED.name());

                    } catch (Exception e) {
                        log.error("[EMAIL] Failed to send notificationId={}: {}",
                                event.getNotificationId(), e.getMessage());
                        notification.setStatus(Notification.Status.FAILED);
                        metricService.recordDelivery(notification.getChannel(), Notification.Status.FAILED.name());
                    }
                    notificationRepository.save(notification);
                    Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
                    metricService.recordDeliveryTime(notification.getChannel(), duration);
                }
        );
    }
}
