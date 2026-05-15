package com.instagram.follow.controller;

import com.instagram.follow.dto.*;
import com.instagram.follow.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Tag(name = "Follows", description = "Follow management APIs")
public class FollowController {

    private final FollowService followService;

    @PostMapping
    @Operation(summary = "Follow a user")
    public ResponseEntity<ApiResponse> followUser(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Username") String username,
            @Valid @RequestBody FollowRequest request) {
        FollowDto follow = followService.followUser(userId, username, request);
        return new ResponseEntity<>(ApiResponse.success("Followed successfully", follow), HttpStatus.CREATED);
    }

    @DeleteMapping("/{followingId}")
    @Operation(summary = "Unfollow a user")
    public ResponseEntity<ApiResponse> unfollowUser(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long followingId) {
        followService.unfollowUser(userId, followingId);
        return ResponseEntity.ok(ApiResponse.success("Unfollowed successfully", null));
    }

    @GetMapping("/followers/{userId}")
    @Operation(summary = "Get followers of a user")
    public ResponseEntity<ApiResponse> getFollowers(@PathVariable Long userId) {
        List<FollowDto> followers = followService.getFollowers(userId);
        return ResponseEntity.ok(ApiResponse.success("Followers fetched successfully", followers));
    }

    @GetMapping("/following/{userId}")
    @Operation(summary = "Get users that a user is following")
    public ResponseEntity<ApiResponse> getFollowing(@PathVariable Long userId) {
        List<FollowDto> following = followService.getFollowing(userId);
        return ResponseEntity.ok(ApiResponse.success("Following fetched successfully", following));
    }

    @GetMapping("/count/{userId}")
    @Operation(summary = "Get follow counts for a user")
    public ResponseEntity<ApiResponse> getFollowCounts(@PathVariable Long userId) {
        FollowCountDto counts = followService.getFollowCounts(userId);
        return ResponseEntity.ok(ApiResponse.success("Follow counts fetched successfully", counts));
    }

    @GetMapping("/check")
    @Operation(summary = "Check if following a user")
    public ResponseEntity<ApiResponse> isFollowing(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long followingId) {
        boolean isFollowing = followService.isFollowing(userId, followingId);
        return ResponseEntity.ok(ApiResponse.success("Check successful", isFollowing));
    }

    @GetMapping("/following-ids/{userId}")
    @Operation(summary = "Get list of following user IDs")
    public ResponseEntity<ApiResponse> getFollowingIds(@PathVariable Long userId) {
        List<Long> ids = followService.getFollowingIds(userId);
        return ResponseEntity.ok(ApiResponse.success("Following IDs fetched successfully", ids));
    }
}
