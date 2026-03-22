package com.neelesh.noftification_service.service;

import com.neelesh.noftification_service.dto.NotificationRequest;
import com.neelesh.noftification_service.enums.Channel;
import com.neelesh.noftification_service.enums.Priority;
import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.model.OutboxEvent;
import com.neelesh.noftification_service.repository.NotificationRepository;
import com.neelesh.noftification_service.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private RateLimiterService rateLimiterService;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationRequest notificationRequest;
    private Notification savedNotification;
    private OutboxEvent savedOutboxEvent;

    @BeforeEach
    void setUp() {
        notificationRequest = new NotificationRequest();
        notificationRequest.setUserId("user123");
        notificationRequest.setChannel(Channel.EMAIL);
        notificationRequest.setPriority(Priority.CRITICAL);
        notificationRequest.setTitle("Test Title");
        notificationRequest.setBody("Test Body");
        notificationRequest.setMetadata(Map.of("key", "value"));

        // IMPORTANT: The saved notification MUST have an ID set
        savedNotification = Notification.builder()
                .id(1L)  // This ID is crucial
                .userId("user123")
                .channel(Channel.EMAIL)
                .priority(Priority.CRITICAL)
                .title("Test Title")
                .body("Test Body")
                .metadata(Map.of("key", "value"))
                .build();

        savedOutboxEvent = OutboxEvent.builder()
                .id(1L)
                .notificationId(1L)  // Must match the notification ID
                .userId("user123")
                .channel(Channel.EMAIL)
                .priority(Priority.CRITICAL)
                .title("Test Title")
                .body("Test Body")
                .metadata(Map.of("key", "value"))
                .build();
    }

    @Test
    @DisplayName("Should save notification and outbox event successfully")
    void testSend_SavesNotificationAndOutboxEvent() {
        // Arrange
        // CRITICAL: Mock must return the saved notification WITH ID
        when(notificationRepository.save(any())).thenReturn(savedNotification);
        when(outboxRepository.save(any(OutboxEvent.class))).thenReturn(savedOutboxEvent);

        // rateLimiterService.checkRateLimit is void, so it just needs to not throw
        doNothing().when(rateLimiterService).checkRateLimit(notificationRequest.getUserId(), notificationRequest.getChannel());

        // Act
        Notification result = notificationService.send(notificationRequest);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(savedNotification.getId(), result.getId());
        assertEquals(savedNotification.getUserId(), result.getUserId());
        assertEquals(savedNotification.getChannel(), result.getChannel());

        verify(rateLimiterService).checkRateLimit(notificationRequest.getUserId(), notificationRequest.getChannel());
        verify(notificationRepository).save(any(Notification.class));
        verify(outboxRepository).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("Should propagate RateLimitExceededException")
    void testSend_PropagatesRateLimitException() {
        // Arrange
        // FIX: Use doThrow().when() for void methods, not when().thenThrow()
        doThrow(new RateLimiterService.RateLimitExceededException("Rate limit exceeded"))
                .when(rateLimiterService)
                .checkRateLimit(notificationRequest.getUserId(), notificationRequest.getChannel());

        // Act & Assert
        RateLimiterService.RateLimitExceededException exception = assertThrows(
                RateLimiterService.RateLimitExceededException.class,
                () -> notificationService.send(notificationRequest)
        );

        assertEquals("Rate limit exceeded", exception.getMessage());
        verify(rateLimiterService).checkRateLimit(notificationRequest.getUserId(), notificationRequest.getChannel());
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(outboxRepository, never()).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("Should create notification with correct data from request")
    void testSend_CreatesNotificationWithCorrectData() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(outboxRepository.save(any(OutboxEvent.class))).thenReturn(savedOutboxEvent);

        // Act
        notificationService.send(notificationRequest);

        // Assert
        verify(notificationRepository).save(argThat(notification ->
            notification.getUserId().equals(notificationRequest.getUserId()) &&
            notification.getChannel().equals(notificationRequest.getChannel()) &&
            notification.getPriority().equals(notificationRequest.getPriority()) &&
            notification.getTitle().equals(notificationRequest.getTitle()) &&
            notification.getBody().equals(notificationRequest.getBody()) &&
            notification.getMetadata().equals(notificationRequest.getMetadata())
        ));
    }

    @Test
    @DisplayName("Should create outbox event with correct data from notification")
    void testSend_CreatesOutboxEventWithCorrectData() {
        // Arrange
        when(notificationRepository.save(any())).thenReturn(savedNotification);
        when(outboxRepository.save(any(OutboxEvent.class))).thenReturn(savedOutboxEvent);

        // Act
        notificationService.send(notificationRequest);

        // Assert - The outbox event should have the saved notification's ID
        verify(outboxRepository).save(argThat(outboxEvent ->
            outboxEvent.getNotificationId() != null &&
            outboxEvent.getNotificationId().equals(savedNotification.getId()) &&
            outboxEvent.getUserId().equals(savedNotification.getUserId()) &&
            outboxEvent.getChannel().equals(savedNotification.getChannel()) &&
            outboxEvent.getPriority().equals(savedNotification.getPriority()) &&
            outboxEvent.getTitle().equals(savedNotification.getTitle()) &&
            outboxEvent.getBody().equals(savedNotification.getBody()) &&
            outboxEvent.getMetadata().equals(savedNotification.getMetadata())
        ));
    }

    @Test
    @DisplayName("Should return notification from repository")
    void testGetNotification_ReturnsNotification() {
        // Arrange
        Long notificationId = 1L;
        when(notificationRepository.findById(notificationId)).thenReturn(java.util.Optional.of(savedNotification));

        // Act
        Notification result = notificationService.getNotification(notificationId);

        // Assert
        assertNotNull(result);
        assertEquals(savedNotification.getId(), result.getId());
        verify(notificationRepository).findById(notificationId);
    }

    @Test
    @DisplayName("Should throw exception when notification not found")
    void testGetNotification_ThrowsExceptionWhenNotFound() {
        // Arrange
        Long notificationId = 999L;
        when(notificationRepository.findById(notificationId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.getNotification(notificationId));

        assertEquals("Notification not found: " + notificationId, exception.getMessage());
        verify(notificationRepository).findById(notificationId);
    }
}