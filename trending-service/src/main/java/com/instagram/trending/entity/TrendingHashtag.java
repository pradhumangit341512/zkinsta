package com.instagram.trending.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trending_hashtags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String hashtag;

    @Column(nullable = false)
    @Builder.Default
    private Long postCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
