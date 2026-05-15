package com.instagram.trending.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingHashtagDto {
    private Long id;
    private String hashtag;
    private Long postCount;
    private Long viewCount;
    private LocalDateTime lastUpdated;
}
