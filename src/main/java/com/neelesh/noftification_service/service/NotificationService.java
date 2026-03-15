package com.neelesh.noftification_service.service;

import com.neelesh.noftification_service.dto.NotificationRequest;
import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.model.OutboxEvent;
import com.neelesh.noftification_service.repository.NotificationRepository;
import com.neelesh.noftification_service.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final OutboxRepository outboxRepository;
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
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .notificationId(notification.getId())
                .userId(notification.getUserId())
                .channel(notification.getChannel())
                .priority(notification.getPriority())
                .title(notification.getTitle())
                .body(notification.getBody())
                .metadata(notification.getMetadata())
                .build();
        outboxRepository.save(outboxEvent);
        return notification;
    }
}
