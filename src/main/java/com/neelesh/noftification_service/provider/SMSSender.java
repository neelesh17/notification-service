package com.neelesh.noftification_service.provider;

import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.model.UserPreferences;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;


@Component
@Slf4j
public class SMSSender {
    @Value("${app.providers.twilio.from-number}")
    String fromNumber;

    @CircuitBreaker(name = "sms", fallbackMethod = "smsFallback")
    @Retry(name = "sms")
    public void send(Notification notification, UserPreferences userPreferences){
        Message message = Message.creator(
                new PhoneNumber(userPreferences.getPhone()),
                new PhoneNumber(fromNumber),
                notification.getBody()
        ).create();
        if (message.getStatus() == Message.Status.FAILED ||
                message.getStatus() == Message.Status.UNDELIVERED) {
            throw new RuntimeException("Twilio delivery failed with status: " + message.getStatus());
        }
        log.info("[SMS] Sent successfully, SID: {}", message.getSid());
    }

    public void smsFallback(Notification notification, UserPreferences userPreferences, Exception e) {
        log.error("[SMS] Circuit open, marking failed: {}", e.getMessage());
        throw new RuntimeException("SMS delivery failed: " + e.getMessage());
    }
}
