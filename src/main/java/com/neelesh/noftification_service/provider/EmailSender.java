package com.neelesh.noftification_service.provider;

import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.model.UserPreferences;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSender {
    @Value("${app.providers.sendgrid.api-key}")
    String apiKey;

    @Value("${app.providers.sendgrid.from-email}")
    String fromEmail;

    @CircuitBreaker(name = "sendgrid", fallbackMethod = "emailFallback")
    @Retry(name = "sendgrid")
    public void send(Notification notification, UserPreferences userPreferences) throws IOException {
        Email from = new Email(fromEmail);
        Email to = new Email(userPreferences.getEmail());
        Content content = new Content("text/plain",notification.getBody());
        Mail mail = new Mail(from, notification.getTitle(), to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request req = new Request();
        req.setMethod(Method.POST);
        req.setEndpoint("mail/send");
        req.setBody(mail.build());
        Response response = sg.api(req);
        if (response.getStatusCode() >= 400) {
            throw new RuntimeException("SendGrid error: " + response.getStatusCode() + " " + response.getBody());
        }
    }

    public void emailFallback(Notification notification, UserPreferences userPreferences, Exception e) {
        log.error("[EMAIL] Circuit open, marking failed: {}", e.getMessage());
        notification.setStatus(Notification.Status.FAILED);
    }
}
