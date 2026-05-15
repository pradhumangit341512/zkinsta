package com.instagram.follow.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowDto {
    private Long id;
    private Long followerId;
    private String followerUsername;
    private Long followingId;
    private String followingUsername;
    private LocalDateTime createdAt;
}
