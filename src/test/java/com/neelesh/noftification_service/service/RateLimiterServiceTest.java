package com.neelesh.noftification_service.service;

import com.neelesh.noftification_service.enums.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimiterService Tests")
public class RateLimiterServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // Set maxRequests to 100 using reflection
        ReflectionTestUtils.setField(rateLimiterService, "maxRequests", 100L);
    }

    @Test
    @DisplayName("Should set expiry when request count is 1")
    void testFirstRequest_ShouldSetExpiry() {
        // Arrange
        String userId = "user123";
        Channel channel = Channel.EMAIL;
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // Act
        rateLimiterService.checkRateLimit(userId, channel);

        // Assert
        verify(redisTemplate).expire("limit:user123:EMAIL", 60, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("Should not throw exception when request count is within limit")
    void testRequestWithinLimit_ShouldNotThrowException() {
        // Arrange
        String userId = "user456";
        Channel channel = Channel.SMS;
        when(valueOperations.increment(anyString())).thenReturn(50L);

        // Act & Assert
        assertDoesNotThrow(() -> rateLimiterService.checkRateLimit(userId, channel));
        verify(redisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should throw RateLimitExceededException when limit is exceeded")
    void testLimitExceeded_ShouldThrowException() {
        // Arrange
        String userId = "user789";
        Channel channel = Channel.PUSH;
        when(valueOperations.increment(anyString())).thenReturn(101L);

        // Act & Assert
        RateLimiterService.RateLimitExceededException exception = assertThrows(
                RateLimiterService.RateLimitExceededException.class,
                () -> rateLimiterService.checkRateLimit(userId, channel)
        );

        assertEquals("Rate limit exceeded for the user: user789", exception.getMessage());
    }

    @Test
    @DisplayName("Should use correct Redis key format")
    void testCorrectRedisKeyFormat() {
        // Arrange
        String userId = "user100";
        Channel channel = Channel.EMAIL;
        when(valueOperations.increment("limit:user100:EMAIL")).thenReturn(1L);

        // Act
        rateLimiterService.checkRateLimit(userId, channel);

        // Assert
        verify(valueOperations).increment("limit:user100:EMAIL");
    }

    @Test
    @DisplayName("Should handle multiple channels for same user independently")
    void testMultipleChannels_ShouldBeIndependent() {
        // Arrange
        String userId = "user200";
        when(valueOperations.increment("limit:user200:EMAIL")).thenReturn(50L);
        when(valueOperations.increment("limit:user200:SMS")).thenReturn(30L);

        // Act
        rateLimiterService.checkRateLimit(userId, Channel.EMAIL);
        rateLimiterService.checkRateLimit(userId, Channel.SMS);

        // Assert
        verify(valueOperations).increment("limit:user200:EMAIL");
        verify(valueOperations).increment("limit:user200:SMS");
    }

    @Test
    @DisplayName("Should throw exception when count equals maxRequests + 1")
    void testCountAtMaxPlusOne_ShouldThrowException() {
        // Arrange
        String userId = "user301";
        Channel channel = Channel.PUSH;
        when(valueOperations.increment(anyString())).thenReturn(101L); // maxRequests = 100

        // Act & Assert
        assertThrows(
                RateLimiterService.RateLimitExceededException.class,
                () -> rateLimiterService.checkRateLimit(userId, channel)
        );
    }

    @Test
    @DisplayName("Should not throw exception when count equals maxRequests")
    void testCountAtMax_ShouldNotThrowException() {
        // Arrange
        String userId = "user302";
        Channel channel = Channel.EMAIL;
        when(valueOperations.increment(anyString())).thenReturn(100L); // exactly maxRequests

        // Act & Assert
        assertDoesNotThrow(() -> rateLimiterService.checkRateLimit(userId, channel));
    }

    @Test
    @DisplayName("Should increment counter for each request")
    void testCounterIncrement() {
        // Arrange
        String userId = "user400";
        Channel channel = Channel.SMS;
        when(valueOperations.increment("limit:user400:SMS")).thenReturn(1L);

        // Act
        rateLimiterService.checkRateLimit(userId, channel);

        // Assert
        verify(valueOperations, times(1)).increment("limit:user400:SMS");
    }

    @Test
    @DisplayName("Should set TTL only on first request")
    void testTtlSetOnlyOnFirstRequest() {
        // Arrange
        String userId = "user500";
        Channel channel = Channel.PUSH;
        String key = "limit:user500:PUSH";

        // First request
        when(valueOperations.increment(key)).thenReturn(1L);
        rateLimiterService.checkRateLimit(userId, channel);

        // Second request
        when(valueOperations.increment(key)).thenReturn(2L);
        rateLimiterService.checkRateLimit(userId, channel);

        // Assert - expire should be called only once
        verify(redisTemplate, times(1)).expire(key, 60, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("Should throw exception with correct message containing userId")
    void testExceptionMessageContainsUserId() {
        // Arrange
        String userId = "userSpecific123";
        Channel channel = Channel.EMAIL;
        when(valueOperations.increment(anyString())).thenReturn(101L);

        // Act & Assert
        RateLimiterService.RateLimitExceededException exception = assertThrows(
                RateLimiterService.RateLimitExceededException.class,
                () -> rateLimiterService.checkRateLimit(userId, channel)
        );

        assertTrue(exception.getMessage().contains(userId));
        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
    }

    @Test
    @DisplayName("Should handle different maxRequests values")
    void testDifferentMaxRequests() {
        // Arrange
        ReflectionTestUtils.setField(rateLimiterService, "maxRequests", 50L);
        String userId = "user600";
        Channel channel = Channel.SMS;
        when(valueOperations.increment(anyString())).thenReturn(51L);

        // Act & Assert
        assertThrows(
                RateLimiterService.RateLimitExceededException.class,
                () -> rateLimiterService.checkRateLimit(userId, channel)
        );
    }

    @Test
    @DisplayName("Should handle all channel types")
    void testAllChannelTypes() {
        // Arrange
        String userId = "user700";
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // Act & Assert - should not throw for any channel
        for (Channel channel : Channel.values()) {
            assertDoesNotThrow(() -> rateLimiterService.checkRateLimit(userId, channel));
        }
    }
}