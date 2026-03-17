package com.neelesh.noftification_service.job;

import com.neelesh.noftification_service.dto.NotificationEvent;
import com.neelesh.noftification_service.kafka.KafkaProducer;
import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RetryNotificationJob implements Job {

    private final NotificationRepository notificationRepository;
    private final KafkaProducer kafkaProducer;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException{
        Long notificationId = context.getJobDetail().getJobDataMap().getLongValue("notificationId");
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getStatus() == Notification.Status.DELAYED) {
                NotificationEvent event = NotificationEvent.builder()
                        .notificationId(notification.getId())
                        .userId(notification.getUserId())
                        .channel(notification.getChannel())
                        .priority(notification.getPriority())
                        .title(notification.getTitle())
                        .body(notification.getBody())
                        .metadata(notification.getMetadata())
                        .createdAt(notification.getCreatedAt())
                        .build();
                kafkaProducer.publish(event);
            }
        });
    }
}
