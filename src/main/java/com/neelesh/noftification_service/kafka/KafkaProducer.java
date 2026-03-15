package com.neelesh.noftification_service.kafka;

import com.neelesh.noftification_service.dto.NotificationEvent;
import com.neelesh.noftification_service.enums.Channel;
import com.neelesh.noftification_service.enums.Priority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${app.kafka.topics.email}")
    private String emailTopic;

    @Value("${app.kafka.topics.sms}")
    private String smsTopic;

    @Value("${app.kafka.topics.push}")
    private String pushTopic;

    private int resolvePartition(Priority priority){
        return switch(priority){
            case CRITICAL -> 0;
            case PROMOTIONAL -> 1;
        };
    }

    private String resolveTopic(Channel channel){
        return switch(channel){
            case EMAIL -> emailTopic;
            case PUSH -> pushTopic;
            case SMS -> smsTopic;
        };
    }

    public void publish(NotificationEvent event){
        String topic = resolveTopic(event.getChannel());
        int partition = resolvePartition(event.getPriority());
        kafkaTemplate.send(topic, partition, event.getUserId(), event)
                .whenComplete((res, ex) -> {
                    if(ex != null){
                        log.error("Failed to publish notification {}: {}",event.getNotificationId(), ex.getMessage());
                    }else{
                        log.info("Published notification {} to topic {} partition {}",
                                event.getNotificationId(), topic, partition);
                    }
                });
    }
}
