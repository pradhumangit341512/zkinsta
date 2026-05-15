package com.instagram.follow.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private Long receiverId;
    private String type;
    private String message;
    private Long referenceId;
    private boolean read;
    private LocalDateTime createdAt;
}
