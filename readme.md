# Overview:

The notification service helps send notifications to consumers based on different types of channels: email, push, or SMS, using a producer-consumer model supported by Kafka. The service rate-limits to 10 notifications per user per hour and has a priority feature which prioritizes sending critical messages backed by Kafka partitions.

# Architecture:

![img.png](img.png)

# Technical Decisions:
1. We used Kafka because it guarantees at least once delivery to a consumer
2. Redis was used for rate limiting as it is a fast cache 
3. Outbox pattern was implemented to solve the dual write problem —
   ensuring a notification saved to DB is always published to Kafka
   even if the app crashes between the two operations.
4. Priority routing via Kafka partitions — CRITICAL messages route to
   partition 0, PROMOTIONAL to partition 1, ensuring critical
   notifications are processed first.
5. Optimistic locking via @Version on Notification entity — prevents
   duplicate status updates when multiple consumer instances process
   the same message.

# Tech Stack:
1. Java 21
2. Spring Boot 3.2.3
3. Apache Kafka — async message streaming, 2 partitions per topic
4. Redis — sub-millisecond rate limiting, sliding window counter
5. MySQL 8 — persistent notification storage with audit log
6. Docker — local infrastructure (Kafka, Redis, MySQL, Zookeeper)
7. Resilience4j — circuit breaker per provider (Week 3)

# Getting Started:
1. Java 21, Maven  compiler and docker installed
2. Setup all environment variables present in env.example file
3. Run docker-compose -d up
4. Wait 30 sec and then run ./mvnw spring-boot:run

# API Reference:
### Send notification(POST): 
url: http://localhost:8080/api/v1/notification/send

sample request body:
{
"userId": "user123",
"channel": "EMAIL",
"priority": "CRITICAL",
"title": "Test",
"body": "Hello from Week 1"
}

sample response:
{
"status": "PENDING",
"message": "Notification queued for delivery",
"notificationId": 2
}

# Project Structure:
- config/         KafkaConfig.java
- enums/          Channel.java, Priority.java 
- model/          Notification.java, OutboxEvent.java
- dto/            NotificationRequest.java, NotificationEvent.java 
- repository/     NotificationRepository.java 
- service/        NotificationService.java, RateLimiterService.java, OutboxPoller
- kafka/          KafkaProducer.java, EmailConsumer.java, PushConsumer.java, SmsConsumer.java 
- controller/     NotificationController.java 
- resources/      application.yml, docker-compose.yml

# Known Limitations:
- Integration tests pending
- Provider stubs only — real SendGrid/FCM/Twilio integration in Week 3
- No DND window support yet

# Upcoming:
- Week 3: Real provider integration (SendGrid, FCM, Twilio) + circuit breaker, integration tests
- Week 4: DND windows, FreeMarker templates, Quartz scheduler
- Week 5: Prometheus metrics, JMeter load test