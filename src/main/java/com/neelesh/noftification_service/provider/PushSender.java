package com.neelesh.noftification_service.provider;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.model.UserPreferences;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class PushSender {
    @CircuitBreaker(name = "fcm", fallbackMethod = "pushFallback")
    @Retry(name = "fcm")
    public void send(Notification notification, UserPreferences userPreferences) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(userPreferences.getFcmToken())
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(notification.getTitle())
                        .setBody(notification.getBody())
                        .build())
                .build();

        String messageId = FirebaseMessaging.getInstance().send(message);
        if(messageId == null || messageId.isEmpty()){
            throw new RuntimeException("FCM returned empty message ID");
        }
        log.info("[PUSH] FCM message sent with ID: {}", messageId);
    }

    public void pushFallback(Notification notification, UserPreferences userPreferences, Exception e) {
        log.error("[PUSH] Circuit open, marking failed: {}", e.getMessage());
        notification.setStatus(Notification.Status.FAILED);
    }
}
