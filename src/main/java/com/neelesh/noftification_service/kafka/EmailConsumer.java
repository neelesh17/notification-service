package com.neelesh.noftification_service.kafka;

import com.neelesh.noftification_service.dto.NotificationEvent;
import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.model.UserPreferences;
import com.neelesh.noftification_service.repository.NotificationRepository;
import com.neelesh.noftification_service.service.UserPreferencesService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final NotificationRepository notificationRepository;
    private final UserPreferencesService userPreferencesService;

    @Value("${app.providers.sendgrid.api-key}")
    String apiKey;

    @Value("${app.providers.sendgrid.from-email}")
    String fromEmail;

    @KafkaListener(
            topics = "${app.kafka.topics.email}",
            groupId = "notification-group"
    )
    public void  consume(NotificationEvent event, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        notificationRepository.findById(event.getNotificationId())
                .ifPresent(notification -> {
                    if (notification.getStatus() == Notification.Status.DELIVERED) {
                        log.info("Already delivered, skipping");
                        return;
                    }
                    try {
                        UserPreferences userPreferences = userPreferencesService.getUserPreferences(notification.getUserId());
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
                        log.info("[EMAIL] Send notificationId={} to userId={}",
                                event.getNotificationId(), event.getUserId());
                        notification.setStatus(Notification.Status.DELIVERED);
                    } catch (Exception e) {
                        log.error("[EMAIL] Failed to send notificationId={}: {}",
                                event.getNotificationId(), e.getMessage());
                        notification.setStatus(Notification.Status.FAILED);
                    }
                    notificationRepository.save(notification);
                }
        );
    }
}
