package com.instagram.post.repository;

import com.instagram.post.entity.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VideoViewRepository extends JpaRepository<VideoView, Long> {

    Optional<VideoView> findByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    boolean existsByPostIdAndUserIdAndViewedAtAfter(Long postId, Long userId, LocalDateTime after);
}
