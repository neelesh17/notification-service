package com.neelesh.noftification_service.kafka;

import com.neelesh.noftification_service.dto.NotificationEvent;
import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.model.UserPreferences;
import com.neelesh.noftification_service.provider.PushSender;
import com.neelesh.noftification_service.repository.NotificationRepository;
import com.neelesh.noftification_service.service.UserPreferencesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PushConsumer {
    private final NotificationRepository notificationRepository;
    private final UserPreferencesService userPreferencesService;
    private final PushSender pushSender;


    @KafkaListener(
            topics = "${app.kafka.topics.push}",
            groupId = "notification-group"
    )
    public void  consume(NotificationEvent event) {
        notificationRepository.findById(event.getNotificationId())
                .ifPresent(notification -> {
                    if (notification.getStatus() == Notification.Status.DELIVERED) {
                        log.info("Already delivered, skipping");
                        return;
                    }
                    try {
                        UserPreferences userPreferences = userPreferencesService.getUserPreferences(notification.getUserId());
                        pushSender.send(notification, userPreferences);
                        log.info("[Push] Received notificationId={} userId={}",
                                event.getNotificationId(), event.getUserId());
                        notification.setStatus(Notification.Status.DELIVERED);
                    } catch (Exception e) {
                        log.error("[PUSH] Failed to send notificationId={}: {}",
                                event.getNotificationId(), e.getMessage());
                        notification.setStatus(Notification.Status.FAILED);
                    }
                    notificationRepository.save(notification);
                });
    }
}
