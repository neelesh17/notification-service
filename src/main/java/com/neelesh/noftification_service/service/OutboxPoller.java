package com.neelesh.noftification_service.service;

import com.neelesh.noftification_service.dto.NotificationEvent;
import com.neelesh.noftification_service.kafka.KafkaProducer;
import com.neelesh.noftification_service.model.OutboxEvent;
import com.neelesh.noftification_service.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {
    private final KafkaProducer kafkaProducer;
    private final OutboxRepository outboxRepository;

    private NotificationEvent toNotificationEvent(OutboxEvent outboxEvent){
        return NotificationEvent.builder()
                .notificationId(outboxEvent.getNotificationId())
                .userId(outboxEvent.getUserId())
                .channel(outboxEvent.getChannel())
                .priority(outboxEvent.getPriority())
                .title(outboxEvent.getTitle())
                .body(outboxEvent.getBody())
                .metadata(outboxEvent.getMetadata())
                .createdAt(outboxEvent.getCreatedAt())
                .build();
    }

    @Scheduled(fixedDelayString = "${app.outbox.poller-delay-ms}")
    @Transactional
    public void pollAndPublish(){
        log.info("Outbox poller running...");
        outboxRepository.findPendingEvents().forEach(outboxEvent -> {
            kafkaProducer.publish(toNotificationEvent(outboxEvent));
            outboxEvent.setStatus(OutboxEvent.Status.PUBLISHED);
            outboxRepository.save(outboxEvent);
        });
    }
}
