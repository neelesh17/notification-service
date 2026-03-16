package com.neelesh.noftification_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPreferences {
    @Id
    @Column(name="user_id", nullable = false)
    String userId;
    @Column(name="email", nullable = false)
    String email;
    @Column(name="name", nullable = false)
    String name;
    @Column(name="phone", nullable = false)
    String phone;
    LocalTime dndStart;
    LocalTime dndEnd;
    Boolean emailEnabled;
    Boolean smsEnabled;
    Boolean pushEnabled;
    String timeZone;
}
