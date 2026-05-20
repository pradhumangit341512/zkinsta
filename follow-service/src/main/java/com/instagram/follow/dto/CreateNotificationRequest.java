package com.instagram.follow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {

    @NotNull
    private Long senderId;

    @NotNull
    private Long receiverId;

    @NotNull
    @Size(max = 50, message = "Notification type must be under 50 characters")
    private String type;

    @NotNull
    @Size(max = 500, message = "Notification message must be under 500 characters")
    private String message;

    private Long referenceId;
}
