package com.instagram.post.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String text;
    private LocalDateTime createdAt;
}
