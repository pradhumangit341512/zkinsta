package com.instagram.follow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {

    @NotNull
    private Long senderId;

    private String senderUsername;

    @NotNull
    private Long receiverId;

    @NotNull
    private String type;

    @NotNull
    private String message;

    private Long referenceId;
}
