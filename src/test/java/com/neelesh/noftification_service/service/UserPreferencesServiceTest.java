package com.neelesh.noftification_service.service;

import com.neelesh.noftification_service.model.UserPreferences;
import com.neelesh.noftification_service.repository.UserPreferencesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPreferencesService Tests")
public class UserPreferencesServiceTest {

    @Mock
    private UserPreferencesRepository userPreferencesRepository;

    @InjectMocks
    private UserPreferencesService userPreferencesService;

    private UserPreferences userPreferences;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = "user123";
        
        userPreferences = UserPreferences.builder()
                .userId(userId)
                .email("user@example.com")
                .name("John Doe")
                .phone("+1234567890")
                .dndStart(LocalTime.of(22, 0))  // 10 PM
                .dndEnd(LocalTime.of(8, 0))     // 8 AM
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(true)
                .timeZone("UTC")
                .fcmToken("fcm_token_123")
                .build();
    }

    // ==================== getUserPreferences Tests ====================

    @Test
    @DisplayName("Should return user preferences when user exists")
    void testGetUserPreferences_UserExists_ReturnsUserPreferences() {
        // Arrange
        when(userPreferencesRepository.findById(userId)).thenReturn(Optional.of(userPreferences));

        // Act
        UserPreferences result = userPreferencesService.getUserPreferences(userId);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(userPreferences.getUserId(), result.getUserId());
        assertEquals(userPreferences.getEmail(), result.getEmail());
        assertEquals(userPreferences.getName(), result.getName());
        assertEquals(userPreferences.getPhone(), result.getPhone());
        assertEquals(userPreferences.getDndStart(), result.getDndStart());
        assertEquals(userPreferences.getDndEnd(), result.getDndEnd());
        assertEquals(userPreferences.getEmailEnabled(), result.getEmailEnabled());
        assertEquals(userPreferences.getSmsEnabled(), result.getSmsEnabled());
        assertEquals(userPreferences.getPushEnabled(), result.getPushEnabled());
        assertEquals(userPreferences.getTimeZone(), result.getTimeZone());
        assertEquals(userPreferences.getFcmToken(), result.getFcmToken());

        verify(userPreferencesRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void testGetUserPreferences_UserNotFound_ThrowsUserNotFoundException() {
        // Arrange
        when(userPreferencesRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserPreferencesService.UserNotFoundException exception = assertThrows(
                UserPreferencesService.UserNotFoundException.class,
                () -> userPreferencesService.getUserPreferences(userId)
        );

        assertTrue(exception.getMessage().contains("User preferences not found for userId"));
        assertTrue(exception.getMessage().contains(userId));
        verify(userPreferencesRepository).findById(userId);
    }

    // ==================== createUserPreferences Tests ====================

    @Test
    @DisplayName("Should create user preferences successfully when user does not exist")
    void testCreateUserPreferences_UserDoesNotExist_SavesSuccessfully() {
        // Arrange
        when(userPreferencesRepository.existsById(userId)).thenReturn(false);

        // Act
        userPreferencesService.createUserPreferences(userPreferences);

        // Assert
        verify(userPreferencesRepository).existsById(userId);
        verify(userPreferencesRepository).save(userPreferences);
    }

    @Test
    @DisplayName("Should save user preferences with correct data")
    void testCreateUserPreferences_SavesWithCorrectData() {
        // Arrange
        when(userPreferencesRepository.existsById(userId)).thenReturn(false);

        // Act
        userPreferencesService.createUserPreferences(userPreferences);

        // Assert
        verify(userPreferencesRepository).save(argThat(prefs ->
            prefs.getUserId().equals(userId) &&
            prefs.getEmail().equals("user@example.com") &&
            prefs.getName().equals("John Doe") &&
            prefs.getPhone().equals("+1234567890") &&
            prefs.getEmailEnabled().equals(true) &&
            prefs.getSmsEnabled().equals(true) &&
            prefs.getPushEnabled().equals(true)
        ));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when user already exists")
    void testCreateUserPreferences_UserAlreadyExists_ThrowsUserAlreadyExistsException() {
        // Arrange
        when(userPreferencesRepository.existsById(userId)).thenReturn(true);

        // Act & Assert
        UserPreferencesService.UserAlreadyExistsException exception = assertThrows(
                UserPreferencesService.UserAlreadyExistsException.class,
                () -> userPreferencesService.createUserPreferences(userPreferences)
        );

        assertTrue(exception.getMessage().contains("User with userId already exists"));
        assertTrue(exception.getMessage().contains(userId));
        verify(userPreferencesRepository).existsById(userId);
        verify(userPreferencesRepository, never()).save(any(UserPreferences.class));
    }

    // ==================== updateUserPreferences Tests ====================

    @Test
    @DisplayName("Should update user preferences successfully when user exists")
    void testUpdateUserPreferences_UserExists_UpdatesSuccessfully() {
        // Arrange
        when(userPreferencesRepository.existsById(userId)).thenReturn(true);

        UserPreferences updatedPreferences = UserPreferences.builder()
                .userId(userId)
                .email("newemail@example.com")
                .name("Jane Doe")
                .phone("+9876543210")
                .dndStart(LocalTime.of(23, 0))
                .dndEnd(LocalTime.of(7, 0))
                .emailEnabled(false)
                .smsEnabled(true)
                .pushEnabled(false)
                .timeZone("Asia/Kolkata")
                .fcmToken("new_fcm_token_456")
                .build();

        // Act
        userPreferencesService.updateUserPreferences(userId, updatedPreferences);

        // Assert
        verify(userPreferencesRepository).existsById(userId);
        verify(userPreferencesRepository).save(argThat(prefs ->
            prefs.getUserId().equals(userId) &&
            prefs.getEmail().equals("newemail@example.com") &&
            prefs.getName().equals("Jane Doe") &&
            prefs.getPhone().equals("+9876543210") &&
            prefs.getEmailEnabled().equals(false) &&
            prefs.getSmsEnabled().equals(true) &&
            prefs.getPushEnabled().equals(false)
        ));
    }

    @Test
    @DisplayName("Should set userId from parameter during update")
    void testUpdateUserPreferences_SetsUserIdFromParameter() {
        // Arrange
        String newUserId = "new_user_id";
        when(userPreferencesRepository.existsById(newUserId)).thenReturn(true);

        // Create preferences without userId (simulating incoming request)
        UserPreferences preferencesToUpdate = UserPreferences.builder()
                .email("user@example.com")
                .name("John Doe")
                .phone("+1234567890")
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(true)
                .timeZone("UTC")
                .build();

        // Act
        userPreferencesService.updateUserPreferences(newUserId, preferencesToUpdate);

        // Assert
        verify(userPreferencesRepository).save(argThat(prefs ->
            prefs.getUserId().equals(newUserId)
        ));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist during update")
    void testUpdateUserPreferences_UserNotFound_ThrowsUserNotFoundException() {
        // Arrange
        when(userPreferencesRepository.existsById(userId)).thenReturn(false);

        UserPreferences updatedPreferences = UserPreferences.builder()
                .userId(userId)
                .email("newemail@example.com")
                .name("Jane Doe")
                .phone("+9876543210")
                .build();

        // Act & Assert
        UserPreferencesService.UserNotFoundException exception = assertThrows(
                UserPreferencesService.UserNotFoundException.class,
                () -> userPreferencesService.updateUserPreferences(userId, updatedPreferences)
        );

        assertTrue(exception.getMessage().contains("User not found"));
        assertTrue(exception.getMessage().contains(userId));
        verify(userPreferencesRepository).existsById(userId);
        verify(userPreferencesRepository, never()).save(any(UserPreferences.class));
    }

    @Test
    @DisplayName("Should update all fields correctly")
    void testUpdateUserPreferences_UpdatesAllFieldsCorrectly() {
        // Arrange
        when(userPreferencesRepository.existsById(userId)).thenReturn(true);

        UserPreferences updatedPreferences = UserPreferences.builder()
                .userId("should_be_overridden")  // Should be replaced with userId parameter
                .email("updated@example.com")
                .name("Updated Name")
                .phone("+1111111111")
                .dndStart(LocalTime.of(21, 30))
                .dndEnd(LocalTime.of(6, 30))
                .emailEnabled(false)
                .smsEnabled(false)
                .pushEnabled(true)
                .timeZone("America/New_York")
                .fcmToken("updated_fcm_token")
                .build();

        // Act
        userPreferencesService.updateUserPreferences(userId, updatedPreferences);

        // Assert
        verify(userPreferencesRepository).save(argThat(prefs ->
            prefs.getUserId().equals(userId) &&  // Should be the parameter, not the input
            prefs.getEmail().equals("updated@example.com") &&
            prefs.getName().equals("Updated Name") &&
            prefs.getPhone().equals("+1111111111") &&
            prefs.getDndStart().equals(LocalTime.of(21, 30)) &&
            prefs.getDndEnd().equals(LocalTime.of(6, 30)) &&
            prefs.getEmailEnabled().equals(false) &&
            prefs.getSmsEnabled().equals(false) &&
            prefs.getPushEnabled().equals(true) &&
            prefs.getTimeZone().equals("America/New_York") &&
            prefs.getFcmToken().equals("updated_fcm_token")
        ));
    }
}

