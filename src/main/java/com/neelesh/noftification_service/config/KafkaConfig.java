package com.neelesh.noftification_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Value("${app.kafka.topics.email}")
    private String emailTopic;

    @Value("${app.kafka.topics.sms}")
    private String smsTopic;

    @Value("${app.kafka.topics.push}")
    private String pushTopic;

    @Bean
    public NewTopic emailTopic(){
        return TopicBuilder.name(emailTopic)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic smsTopic(){
        return TopicBuilder.name(smsTopic)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic pushTopic(){
        return TopicBuilder.name(pushTopic)
                .partitions(2)
                .replicas(1)
                .build();
    }
}
