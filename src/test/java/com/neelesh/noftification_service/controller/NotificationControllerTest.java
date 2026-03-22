package com.neelesh.noftification_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neelesh.noftification_service.dto.NotificationRequest;
import com.neelesh.noftification_service.enums.Channel;
import com.neelesh.noftification_service.enums.Priority;
import com.neelesh.noftification_service.model.Notification;
import com.neelesh.noftification_service.service.NotificationService;
import com.neelesh.noftification_service.service.RateLimiterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@DisplayName("NotificationController Tests")
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Should return 202 ACCEPTED on successful notification send")
    void testSend_Returns202OnSuccess() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId("user123");
        request.setChannel(Channel.EMAIL);
        request.setPriority(Priority.CRITICAL);
        request.setTitle("Test Title");
        request.setBody("Test Body");

        Notification notification = Notification.builder()
                .id(1L)
                .userId("user123")
                .channel(Channel.EMAIL)
                .priority(Priority.CRITICAL)
                .title("Test Title")
                .body("Test Body")
                .status(Notification.Status.PENDING)
                .build();

        when(notificationService.send(any(NotificationRequest.class))).thenReturn(notification);

        // Act & Assert
        mockMvc.perform(post("/api/v1/notification/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.notificationId").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Notification queued for delivery"));
    }

    @Test
    @DisplayName("Should return 429 TOO_MANY_REQUESTS on rate limit exceeded")
    void testSend_Returns429OnRateLimitExceeded() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId("user123");
        request.setChannel(Channel.EMAIL);
        request.setPriority(Priority.CRITICAL);
        request.setTitle("Test Title");
        request.setBody("Test Body");

        when(notificationService.send(any(NotificationRequest.class)))
                .thenThrow(new RateLimiterService.RateLimitExceededException("Rate limit exceeded"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/notification/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("Please try again after 1 hour"));
    }

    @Test
    @DisplayName("Should return 400 BAD_REQUEST when userId is missing")
    void testSend_Returns400WhenUserIdMissing() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        // userId is missing - @NotBlank validation
        request.setChannel(Channel.EMAIL);
        request.setBody("Test Body");

        // Act & Assert
        mockMvc.perform(post("/api/v1/notification/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 BAD_REQUEST when channel is missing")
    void testSend_Returns400WhenChannelMissing() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId("user123");
        // channel is missing - @NotNull validation
        request.setBody("Test Body");

        // Act & Assert
        mockMvc.perform(post("/api/v1/notification/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 BAD_REQUEST when body is missing")
    void testSend_Returns400WhenBodyMissing() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId("user123");
        request.setChannel(Channel.EMAIL);
        // body is missing - @NotBlank validation

        // Act & Assert
        mockMvc.perform(post("/api/v1/notification/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 200 OK when getting existing notification")
    void testGetNotification_Returns200OnSuccess() throws Exception {
        // Arrange
        Long notificationId = 1L;
        Notification notification = Notification.builder()
                .id(notificationId)
                .userId("user123")
                .channel(Channel.EMAIL)
                .priority(Priority.CRITICAL)
                .title("Test Title")
                .body("Test Body")
                .status(Notification.Status.DELIVERED)
                .build();

        when(notificationService.getNotification(notificationId)).thenReturn(notification);

        // Act & Assert
        mockMvc.perform(get("/api/v1/notification/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.channel").value("EMAIL"))
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    @DisplayName("Should return 404 NOT_FOUND when notification not found")
    void testGetNotification_Returns404WhenNotFound() throws Exception {
        // Arrange
        Long notificationId = 999L;
        when(notificationService.getNotification(notificationId))
                .thenThrow(new RuntimeException("Notification not found: " + notificationId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/notification/{id}", notificationId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Notification not found: " + notificationId));
    }

    @Test
    @DisplayName("Should return 500 INTERNAL_SERVER_ERROR on unexpected exception")
    void testGetNotification_Returns500OnUnexpectedException() throws Exception {
        // Arrange
        Long notificationId = 1L;
        when(notificationService.getNotification(notificationId))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/notification/{id}", notificationId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error"));
    }
}
