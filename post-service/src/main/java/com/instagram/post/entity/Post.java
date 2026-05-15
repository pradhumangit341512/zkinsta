package com.instagram.post.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private String username;

    @Column(length = 2000)
    private String caption;

    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Privacy privacy = Privacy.PUBLIC;

    private String filter;

    @ElementCollection
    @CollectionTable(name = "post_hashtags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "hashtag")
    @Builder.Default
    private List<String> hashtags = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Long likesCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long viewsCount = 0L;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum MediaType {
        IMAGE, VIDEO, TEXT
    }

    public enum Privacy {
        PUBLIC, FRIENDS_ONLY, PRIVATE
    }
}
