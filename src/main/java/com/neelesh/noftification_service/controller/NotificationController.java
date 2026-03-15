package com.neelesh.noftification_service.controller;

import com.neelesh.noftification_service.dto.NotificationRequest;
import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.service.NotificationService;
import com.neelesh.noftification_service.service.RateLimiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/notification/send")
    ResponseEntity<?> send(@Valid @RequestBody NotificationRequest req){
        try{
            Notification notification = notificationService.send(req);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                    "notificationId", notification.getId(),
                    "status", notification.getStatus(),
                    "message", "Notification queued for delivery"
            ));
        }catch(RateLimiterService.RateLimitExceededException e){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Please try again after 1 hour");
        }
    }
}
