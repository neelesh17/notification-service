package com.neelesh.noftification_service.repository;

import com.neelesh.noftification_service.model.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, String> {

}
