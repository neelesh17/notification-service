package com.neelesh.noftification_service.kafka;

import com.neelesh.noftification_service.dto.NotificationEvent;
import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PushConsumer {
    private final NotificationRepository notificationRepository;

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
                        log.info("[Push] Received notificationId={} userId={}",
                                event.getNotificationId(), event.getUserId());
                        notification.setStatus(Notification.Status.DELIVERED);
                    } catch (Exception e) {
                        notification.setStatus(Notification.Status.FAILED);
                    }
                    notificationRepository.save(notification);
                });
    }
}
