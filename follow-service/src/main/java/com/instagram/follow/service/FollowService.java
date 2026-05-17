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
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;
    private final WebClient.Builder webClientBuilder;

    private String fetchUsername(Long userId) {
        try {
            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri("http://authentication-service/api/auth/users/{userId}", userId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (response != null && response.get("data") != null) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return (String) data.get("username");
            }
        } catch (Exception e) {
            log.warn("Failed to fetch username for userId {}: {}", userId, e.getMessage());
        }
        return "user_" + userId;
    }

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
                .followingId(request.getFollowingId())
                .build();

        Follow saved = followRepository.save(follow);

        // Fetch following username via WebClient
        String followingUsername = fetchUsername(request.getFollowingId());

        // Create follow notification
        try {
            notificationService.createNotification(CreateNotificationRequest.builder()
                    .senderId(followerId)
                    .receiverId(request.getFollowingId())
                    .type("FOLLOW")
                    .message(followerUsername + " started following you")
                    .referenceId(saved.getId())
                    .build());
        } catch (Exception e) {
            log.warn("Failed to create follow notification: {}", e.getMessage());
        }

        FollowDto dto = modelMapper.map(saved, FollowDto.class);
        dto.setFollowerUsername(followerUsername);
        dto.setFollowingUsername(followingUsername);
        return dto;
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
                .map(follow -> {
                    FollowDto dto = modelMapper.map(follow, FollowDto.class);
                    dto.setFollowerUsername(fetchUsername(follow.getFollowerId()));
                    dto.setFollowingUsername(fetchUsername(follow.getFollowingId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<FollowDto> getFollowing(Long userId) {
        return followRepository.findByFollowerId(userId).stream()
                .map(follow -> {
                    FollowDto dto = modelMapper.map(follow, FollowDto.class);
                    dto.setFollowerUsername(fetchUsername(follow.getFollowerId()));
                    dto.setFollowingUsername(fetchUsername(follow.getFollowingId()));
                    return dto;
                })
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
