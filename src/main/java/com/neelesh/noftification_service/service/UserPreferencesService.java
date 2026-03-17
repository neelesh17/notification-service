package com.neelesh.noftification_service.service;

import com.neelesh.noftification_service.model.UserPreferences;
import com.neelesh.noftification_service.repository.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPreferencesService {
    private final UserPreferencesRepository userPreferencesRepository;

    public UserPreferences getUserPreferences(String userId){
        return userPreferencesRepository.findById(userId).
                orElseThrow(() -> new UserNotFoundException("User preferences not found for userId: " + userId));
    }

    public void createUserPreferences(UserPreferences userPreferences){
        if(userPreferencesRepository.existsById(userPreferences.getUserId())){
            throw new UserAlreadyExistsException("User with userId already exists: " + userPreferences.getUserId());
        }
        userPreferencesRepository.save(userPreferences);
    }

    public void updateUserPreferences(String userId, UserPreferences userPreferences) {
        if (!userPreferencesRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        userPreferences.setUserId(userId);
        userPreferencesRepository.save(userPreferences);
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class UserAlreadyExistsException extends RuntimeException{
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }
}
