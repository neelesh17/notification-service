package com.neelesh.noftification_service.kafka;

import com.neelesh.noftification_service.dto.NotificationEvent;
import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final NotificationRepository notificationRepository;

    @KafkaListener(
            topics = "${app.kafka.topics.email}",
            groupId = "notification-group"
    )
    public void  consume(NotificationEvent event, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        notificationRepository.findById(event.getNotificationId())
                .ifPresent(notification -> {
                    try {
                        log.info("[EMAIL] Received notificationId={} userId={} partition={}",
                                event.getNotificationId(), event.getUserId(), partition);
                        notification.setStatus(Notification.Status.DELIVERED);
                    } catch (Exception e) {
                        notification.setStatus(Notification.Status.FAILED);
                    }
                    notificationRepository.save(notification);
                }
        );
    }
}
