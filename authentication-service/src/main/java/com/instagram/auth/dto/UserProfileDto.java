package com.instagram.auth.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    private Long id;
    private String fullName;
    private String email;
    private String username;
    private String bio;
    private String profilePicture;
    private LocalDateTime createdAt;
}
