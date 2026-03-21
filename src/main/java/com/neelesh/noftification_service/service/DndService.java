package com.neelesh.noftification_service.service;

import com.neelesh.noftification_service.model.UserPreferences;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;

@Service
@Slf4j
public class DndService {
    private boolean isInDnd(LocalTime current, LocalTime start, LocalTime end) {
        if (start.isAfter(end)) {
            // crosses midnight — two ranges
            return current.isAfter(start) || current.isBefore(end);
        } else {
            // same day window e.g. 09:00 to 17:00
            return current.isAfter(start) && current.isBefore(end);
        }
    }

    public boolean isDndActive(UserPreferences userPreferences){
        String timezone = userPreferences.getTimeZone() != null ? userPreferences.getTimeZone() : "UTC";
        LocalTime now = LocalTime.now(ZoneId.of(timezone));
        LocalTime dndStart = userPreferences.getDndStart();
        LocalTime dndEnd = userPreferences.getDndEnd();
        return dndStart != null && dndEnd != null && isInDnd(now, dndStart, dndEnd);
    }

    // Testable version
    public boolean isDndActive(UserPreferences userPreferences, LocalTime currentTime) {
        LocalTime dndStart = userPreferences.getDndStart();
        LocalTime dndEnd = userPreferences.getDndEnd();
        return dndStart != null && dndEnd != null && isInDnd(currentTime, dndStart, dndEnd);
    }
}
