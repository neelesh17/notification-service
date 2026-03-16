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
                orElseThrow(() -> new RuntimeException("User preferences not found for userId: " + userId));
    }

    public void createUserPreferences(UserPreferences userPreferences){
        userPreferencesRepository.save(userPreferences);
    }
}
