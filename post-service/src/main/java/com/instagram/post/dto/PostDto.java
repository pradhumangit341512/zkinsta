package com.instagram.post.dto;

import com.instagram.post.entity.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
    private Long id;
    private Long userId;
    private String username;
    private String caption;
    private String mediaUrl;
    private Post.MediaType mediaType;
    private Post.Privacy privacy;
    private String filter;
    private List<String> hashtags;
    private Long likesCount;
    private Long viewsCount;
    private Long commentsCount;
    private boolean likedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
