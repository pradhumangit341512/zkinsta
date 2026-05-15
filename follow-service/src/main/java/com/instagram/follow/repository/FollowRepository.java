package com.instagram.follow.repository;

import com.instagram.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    List<Follow> findByFollowingId(Long followingId);

    List<Follow> findByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);

    long countByFollowerId(Long followerId);
}
