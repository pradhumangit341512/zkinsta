package com.instagram.follow.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowCountDto {
    private Long userId;
    private long followersCount;
    private long followingCount;
}
