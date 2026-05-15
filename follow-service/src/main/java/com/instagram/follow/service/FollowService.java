package com.instagram.follow.service;

import com.instagram.follow.dto.*;
import com.instagram.follow.entity.Follow;
import com.instagram.follow.exception.CustomException;
import com.instagram.follow.repository.FollowRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    @CircuitBreaker(name = "followService", fallbackMethod = "followUserFallback")
    public FollowDto followUser(Long followerId, String followerUsername, FollowRequest request) {
        if (followerId.equals(request.getFollowingId())) {
            throw new CustomException("You cannot follow yourself", HttpStatus.BAD_REQUEST);
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, request.getFollowingId())) {
            throw new CustomException("You are already following this user", HttpStatus.CONFLICT);
        }

        Follow follow = Follow.builder()
                .followerId(followerId)
                .followerUsername(followerUsername)
                .followingId(request.getFollowingId())
                .followingUsername(request.getFollowingUsername())
                .build();

        Follow saved = followRepository.save(follow);

        // Create follow notification
        try {
            notificationService.createNotification(CreateNotificationRequest.builder()
                    .senderId(followerId)
                    .senderUsername(followerUsername)
                    .receiverId(request.getFollowingId())
                    .type("FOLLOW")
                    .message(followerUsername + " started following you")
                    .referenceId(saved.getId())
                    .build());
        } catch (Exception e) {
            log.warn("Failed to create follow notification: {}", e.getMessage());
        }

        return modelMapper.map(saved, FollowDto.class);
    }

    public FollowDto followUserFallback(Long followerId, String followerUsername, FollowRequest request, Throwable t) {
        if (t instanceof CustomException) throw (CustomException) t;
        log.error("Circuit breaker fallback for followUser: {}", t.getMessage());
        throw new CustomException("Service temporarily unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    public void unfollowUser(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new CustomException("You are not following this user", HttpStatus.BAD_REQUEST));
        followRepository.delete(follow);
    }

    public List<FollowDto> getFollowers(Long userId) {
        return followRepository.findByFollowingId(userId).stream()
                .map(follow -> modelMapper.map(follow, FollowDto.class))
                .collect(Collectors.toList());
    }

    public List<FollowDto> getFollowing(Long userId) {
        return followRepository.findByFollowerId(userId).stream()
                .map(follow -> modelMapper.map(follow, FollowDto.class))
                .collect(Collectors.toList());
    }

    public FollowCountDto getFollowCounts(Long userId) {
        long followersCount = followRepository.countByFollowingId(userId);
        long followingCount = followRepository.countByFollowerId(userId);
        return FollowCountDto.builder()
                .userId(userId)
                .followersCount(followersCount)
                .followingCount(followingCount)
                .build();
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    public List<Long> getFollowingIds(Long userId) {
        return followRepository.findByFollowerId(userId).stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toList());
    }
}
