package com.instagram.post.repository;

import com.instagram.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Post> findByUserIdInAndPrivacyOrderByCreatedAtDesc(List<Long> userIds, Post.Privacy privacy, Pageable pageable);

    Page<Post> findByPrivacyOrderByCreatedAtDesc(Post.Privacy privacy, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.privacy = 'PUBLIC' ORDER BY p.likesCount DESC, p.viewsCount DESC")
    Page<Post> findTrendingPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.privacy = 'PUBLIC' AND p.createdAt >= :since ORDER BY p.likesCount DESC")
    Page<Post> findTrendingPostsSince(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.hashtags h WHERE LOWER(h) = LOWER(:hashtag) AND p.privacy = 'PUBLIC' ORDER BY p.createdAt DESC")
    Page<Post> findByHashtag(@Param("hashtag") String hashtag, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE (LOWER(p.caption) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.username) LIKE LOWER(CONCAT('%', :query, '%'))) AND p.privacy = 'PUBLIC' ORDER BY p.createdAt DESC")
    Page<Post> searchPosts(@Param("query") String query, Pageable pageable);

    List<Post> findByUserIdIn(List<Long> userIds);
}
