package com.instagram.trending.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingPostDto {
    private Long id;
    private Long userId;
    private String username;
    private String caption;
    private String mediaUrl;
    private String mediaType;
    private Long likesCount;
    private Long viewsCount;
    private List<String> hashtags;
    private LocalDateTime createdAt;
}
