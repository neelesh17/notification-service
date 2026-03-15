package com.neelesh.noftification_service.model;

import com.neelesh.noftification_service.enums.Channel;
import com.neelesh.noftification_service.enums.Priority;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="outbox")
@Getter
@Setter
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name="user_id", nullable=false)
    String userId;
    @Column(name = "notification_id", nullable = false)
    Long notificationId;
    @Enumerated(EnumType.STRING)
    Channel channel;
    @Enumerated(EnumType.STRING)
    Priority priority;
    String title;
    @Column(name="body", nullable=false)
    String body;
    @Enumerated(EnumType.STRING)
    OutboxEvent.Status status;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, String> metadata;
    LocalDateTime createdAt;

    public enum Status {
        PENDING,PUBLISHED
    }
    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        if(status == null) status  = OutboxEvent.Status.PENDING;
    }
}
