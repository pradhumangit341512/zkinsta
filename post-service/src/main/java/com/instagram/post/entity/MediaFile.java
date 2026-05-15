package com.instagram.post.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "media_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private String filename;

    @Lob
    @Column(name = "data", nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] data;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
