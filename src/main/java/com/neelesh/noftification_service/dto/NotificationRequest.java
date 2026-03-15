package com.neelesh.noftification_service.dto;

import com.neelesh.noftification_service.enums.Channel;
import com.neelesh.noftification_service.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class NotificationRequest {
    @NotBlank(message = "user Id is required")
    String userId;
    @NotNull(message = "channel is required")
    Channel channel;
    Priority priority = Priority.PROMOTIONAL;
    String title;
    @NotBlank(message = "body is required")
    String body;
    String templateId;
    Map<String,String> templateVars;
    Map<String, String> metadata;
}
