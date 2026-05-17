package com.instagram.follow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowRequest {

    @NotNull(message = "Please provide a valid following user ID")
    private Long followingId;
}
