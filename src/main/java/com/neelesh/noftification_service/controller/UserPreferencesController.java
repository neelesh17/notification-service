package com.neelesh.noftification_service.controller;

import com.neelesh.noftification_service.model.UserPreferences;
import com.neelesh.noftification_service.service.UserPreferencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserPreferencesController {
    private final UserPreferencesService userPreferencesService;

    @GetMapping("/{userId}")
    ResponseEntity<?> getUsersPreferences(@PathVariable String userId){
        try{
            UserPreferences userPreferences = userPreferencesService.getUserPreferences(userId);
            return ResponseEntity.ok(userPreferences);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("User with user id not found: "+ userId);
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("An error occurred while fetching user: "+ e);
        }
    }
    @PostMapping("/")
    ResponseEntity<?> setUserPreferences(@Valid @RequestBody UserPreferences userPreferences){
        try{
            userPreferencesService.createUserPreferences(userPreferences);
            return ResponseEntity.status(HttpStatus.CREATED).body(userPreferences);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while creating user: "+ e);
        }
    }
}
