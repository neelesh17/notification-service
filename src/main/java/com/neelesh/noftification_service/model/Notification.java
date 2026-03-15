package com.neelesh.noftification_service.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.neelesh.noftification_service.enums.Channel;
import com.neelesh.noftification_service.enums.Priority;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name="notification")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name="user_id", nullable=false)
    String userId;
    @Enumerated(EnumType.STRING)
    Channel channel;
    @Enumerated(EnumType.STRING)
    Priority priority;
    String title;
    @Column(name="body", nullable=false)
    String body;
    @Enumerated(EnumType.STRING)
    Status status;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, String> metadata;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public enum Status {
        PENDING,DELIVERED,FAILED,DELAYED
    }

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if(status == null) status  = Status.PENDING;
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
