package com.neelesh.noftification_service.controller;

import com.neelesh.noftification_service.dto.NotificationRequest;
import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.service.NotificationService;
import com.neelesh.noftification_service.service.RateLimiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/notification/{id}")
    ResponseEntity<?> getNotification(@PathVariable Long id){
        try {
            Notification notification = notificationService.getNotification(id);
            return ResponseEntity.status(HttpStatus.OK).body(notification);
        }catch (RuntimeException e){
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
