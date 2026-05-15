package com.instagram.post.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "video_views", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private Integer watchedDuration;

    @Column(nullable = false, updatable = false)
    private LocalDateTime viewedAt;

    @PrePersist
    protected void onCreate() {
        viewedAt = LocalDateTime.now();
    }
}
