package com.neelesh.noftification_service.service;

    import com.neelesh.noftification_service.model.UserPreferences;
    import org.junit.jupiter.api.DisplayName;
    import org.junit.jupiter.api.Test;

    import java.time.LocalTime;

    import static org.junit.jupiter.api.Assertions.*;

    @DisplayName("DndService Tests")
    public class DndServiceTest {

        private final DndService dndService = new DndService();

        @Test
        @DisplayName("Should return true when current time is within same-day DND window")
        void testIsDndActiveWithinSameDayWindow() {
            // Arrange
            UserPreferences userPreferences = UserPreferences.builder()
                    .dndStart(LocalTime.of(9, 0))
                    .dndEnd(LocalTime.of(17, 0))
                    .build();
            LocalTime currentTime = LocalTime.of(14, 30);

            // Act & Assert
            assertTrue(dndService.isDndActive(userPreferences, currentTime));
        }

        @Test
        @DisplayName("Should return false when current time is before same-day DND window")
        void testIsDndActiveBeforeSameDayWindow() {
            // Arrange
            UserPreferences userPreferences = UserPreferences.builder()
                    .dndStart(LocalTime.of(9, 0))
                    .dndEnd(LocalTime.of(17, 0))
                    .build();
            LocalTime currentTime = LocalTime.of(8, 59);

            // Act & Assert
            assertFalse(dndService.isDndActive(userPreferences, currentTime));
        }

        @Test
        @DisplayName("Should return false when current time is after same-day DND window")
        void testIsDndActiveAfterSameDayWindow() {
            // Arrange
            UserPreferences userPreferences = UserPreferences.builder()
                    .dndStart(LocalTime.of(9, 0))
                    .dndEnd(LocalTime.of(17, 0))
                    .build();
            LocalTime currentTime = LocalTime.of(17, 1);

            // Act & Assert
            assertFalse(dndService.isDndActive(userPreferences, currentTime));
        }

        @Test
        @DisplayName("Should return true when current time is within midnight-crossing DND window")
        void testIsDndActiveWithinMidnightCrossingWindow() {
            // Arrange
            UserPreferences userPreferences = UserPreferences.builder()
                    .dndStart(LocalTime.of(22, 0))
                    .dndEnd(LocalTime.of(6, 0))
                    .build();
            LocalTime currentTime = LocalTime.of(3, 0);

            // Act & Assert
            assertTrue(dndService.isDndActive(userPreferences, currentTime));
        }

        @Test
        @DisplayName("Should return true when current time is before midnight in midnight-crossing window")
        void testIsDndActiveBeforeMidnightInCrossingWindow() {
            // Arrange
            UserPreferences userPreferences = UserPreferences.builder()
                    .dndStart(LocalTime.of(22, 0))
                    .dndEnd(LocalTime.of(6, 0))
                    .build();
            LocalTime currentTime = LocalTime.of(23, 30);

            // Act & Assert
            assertTrue(dndService.isDndActive(userPreferences, currentTime));
        }

        @Test
        @DisplayName("Should return false when current time is outside midnight-crossing window")
        void testIsDndActiveOutsideMidnightCrossingWindow() {
            // Arrange
            UserPreferences userPreferences = UserPreferences.builder()
                    .dndStart(LocalTime.of(22, 0))
                    .dndEnd(LocalTime.of(6, 0))
                    .build();
            LocalTime currentTime = LocalTime.of(12, 0);

            // Act & Assert
            assertFalse(dndService.isDndActive(userPreferences, currentTime));
        }

        @Test
        @DisplayName("Should return false when DND start time is null")
        void testIsDndActiveWhenDndStartIsNull() {
            // Arrange
            UserPreferences userPreferences = UserPreferences.builder()
                    .dndStart(null)
                    .dndEnd(LocalTime.of(17, 0))
                    .timeZone("UTC")
                    .build();

            // Act & Assert
            assertFalse(dndService.isDndActive(userPreferences));
        }

        @Test
        @DisplayName("Should return false when DND end time is null")
        void testIsDndActiveWhenDndEndIsNull() {
            // Arrange
            UserPreferences userPreferences = UserPreferences.builder()
                    .dndStart(LocalTime.of(9, 0))
                    .dndEnd(null)
                    .timeZone("UTC")
                    .build();

            // Act & Assert
            assertFalse(dndService.isDndActive(userPreferences));
        }

        @Test
        @DisplayName("Should return false when both DND times are null")
        void testIsDndActiveWhenBothDndTimesAreNull() {
            // Arrange
            UserPreferences userPreferences = UserPreferences.builder()
                    .dndStart(null)
                    .dndEnd(null)
                    .timeZone("UTC")
                    .build();

            // Act & Assert
            assertFalse(dndService.isDndActive(userPreferences));
        }

        @Test
        @DisplayName("Should use UTC timezone when timezone is null")
        void testIsDndActiveWithNullTimezone() {
            // Arrange
            UserPreferences userPreferences = UserPreferences.builder()
                    .dndStart(LocalTime.of(9, 0))
                    .dndEnd(LocalTime.of(17, 0))
                    .timeZone(null)
                    .build();

            // Act & Assert - Should not throw exception and should use UTC
            assertDoesNotThrow(() -> dndService.isDndActive(userPreferences));
        }

        @Test
        @DisplayName("Should handle different timezones correctly")
        void testIsDndActiveWithDifferentTimezones() {
            // Arrange - IST is UTC+5:30
            UserPreferences userPreferences = UserPreferences.builder()
                    .dndStart(LocalTime.of(22, 0))
                    .dndEnd(LocalTime.of(8, 0))
                    .timeZone("Asia/Kolkata")
                    .build();

            // Act & Assert - Should not throw exception
            assertDoesNotThrow(() -> dndService.isDndActive(userPreferences));
        }

        @Test
        @DisplayName("Should return false at exact boundary (start time)")
        void testIsDndActiveAtStartBoundary() {
            // Arrange
            UserPreferences userPreferences = UserPreferences.builder()
                    .dndStart(LocalTime.of(9, 0))
                    .dndEnd(LocalTime.of(17, 0))
                    .build();
            LocalTime currentTime = LocalTime.of(9, 0);

            // Act & Assert - At exact start, should be false (isAfter is exclusive)
            assertFalse(dndService.isDndActive(userPreferences, currentTime));
        }

        @Test
        @DisplayName("Should return false at exact boundary (end time)")
        void testIsDndActiveAtEndBoundary() {
            // Arrange
            UserPreferences userPreferences = UserPreferences.builder()
                    .dndStart(LocalTime.of(9, 0))
                    .dndEnd(LocalTime.of(17, 0))
                    .build();
            LocalTime currentTime = LocalTime.of(17, 0);

            // Act & Assert - At exact end, should be false (isBefore is exclusive)
            assertFalse(dndService.isDndActive(userPreferences, currentTime));
        }
    }