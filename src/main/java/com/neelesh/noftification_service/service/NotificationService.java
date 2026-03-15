package com.neelesh.noftification_service.service;

import com.neelesh.noftification_service.dto.NotificationEvent;
import com.neelesh.noftification_service.dto.NotificationRequest;
import com.neelesh.noftification_service.kafka.KafkaProducer;
import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final KafkaProducer kafkaProducer;
    private final RateLimiterService rateLimiterService;

    @Transactional
    public Notification send(NotificationRequest notificationRequest){
        rateLimiterService.checkRateLimit(notificationRequest.getUserId(), notificationRequest.getChannel());
        Notification notification = Notification.builder()
                .userId(notificationRequest.getUserId())
                .body(notificationRequest.getBody())
                .title(notificationRequest.getTitle())
                .priority(notificationRequest.getPriority())
                .channel(notificationRequest.getChannel())
                .metadata(notificationRequest.getMetadata())
                .build();
        notificationRepository.save(notification);
        NotificationEvent event = NotificationEvent.builder()
                .notificationId(notification.getId())
                .userId(notification.getUserId())
                .body(notification.getBody())
                .title(notification.getTitle())
                .priority(notification.getPriority())
                .channel(notification.getChannel())
                .metadata(notification.getMetadata())
                .createdAt(LocalDateTime.now())
                .build();
        kafkaProducer.publish(event);
        return notification;
    }
}
