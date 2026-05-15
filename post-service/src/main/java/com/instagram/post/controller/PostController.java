package com.instagram.post.controller;

import com.instagram.post.dto.*;
import com.instagram.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.instagram.post.dto.CommentDto;
import com.instagram.post.dto.CreateCommentRequest;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post management APIs")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "Create a new post")
    public ResponseEntity<ApiResponse> createPost(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Username") String username,
            @Valid @RequestBody CreatePostRequest request) {
        PostDto post = postService.createPost(userId, username, request);
        return new ResponseEntity<>(ApiResponse.success("Post created successfully", post), HttpStatus.CREATED);
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Get a post by ID")
    public ResponseEntity<ApiResponse> getPost(
            @PathVariable Long postId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        PostDto post = postService.getPost(postId, userId);
        return ResponseEntity.ok(ApiResponse.success("Post fetched successfully", post));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all posts by a user")
    public ResponseEntity<ApiResponse> getPostsByUser(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        List<PostDto> posts = postService.getPostsByUser(userId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Posts fetched successfully", posts));
    }

    @GetMapping("/feed")
    @Operation(summary = "Get feed posts from followed users")
    public ResponseEntity<ApiResponse> getFeed(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam List<Long> followingIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PostDto> posts = postService.getFeed(followingIds, userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Feed fetched successfully", posts));
    }

    @GetMapping("/public")
    @Operation(summary = "Get public feed")
    public ResponseEntity<ApiResponse> getPublicFeed(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PostDto> posts = postService.getPublicFeed(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Public feed fetched successfully", posts));
    }

    @PutMapping("/{postId}")
    @Operation(summary = "Update a post")
    public ResponseEntity<ApiResponse> updatePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdatePostRequest request) {
        PostDto post = postService.updatePost(postId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Post updated successfully", post));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Delete a post")
    public ResponseEntity<ApiResponse> deletePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId) {
        postService.deletePost(postId, userId);
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully", null));
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "Like a post")
    public ResponseEntity<ApiResponse> likePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Username") String username) {
        PostDto post = postService.likePost(postId, userId, username);
        return ResponseEntity.ok(ApiResponse.success("Post liked successfully", post));
    }

    @DeleteMapping("/{postId}/like")
    @Operation(summary = "Unlike a post")
    public ResponseEntity<ApiResponse> unlikePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId) {
        PostDto post = postService.unlikePost(postId, userId);
        return ResponseEntity.ok(ApiResponse.success("Post unliked successfully", post));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending posts")
    public ResponseEntity<ApiResponse> getTrendingPosts(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PostDto> posts = postService.getTrendingPosts(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Trending posts fetched successfully", posts));
    }

    @GetMapping("/trending/recent")
    @Operation(summary = "Get trending posts from recent hours")
    public ResponseEntity<ApiResponse> getTrendingPostsRecent(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PostDto> posts = postService.getTrendingPostsSince(userId, hours, page, size);
        return ResponseEntity.ok(ApiResponse.success("Recent trending posts fetched successfully", posts));
    }

    @GetMapping("/hashtag/{hashtag}")
    @Operation(summary = "Get posts by hashtag")
    public ResponseEntity<ApiResponse> getPostsByHashtag(
            @PathVariable String hashtag,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PostDto> posts = postService.getPostsByHashtag(hashtag, userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Posts fetched successfully", posts));
    }

    @GetMapping("/search")
    @Operation(summary = "Search posts")
    public ResponseEntity<ApiResponse> searchPosts(
            @RequestParam String query,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PostDto> posts = postService.searchPosts(query, userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search results fetched successfully", posts));
    }

    @PostMapping("/{postId}/view")
    @Operation(summary = "Record a video view")
    public ResponseEntity<ApiResponse> recordVideoView(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") Integer watchedDuration) {
        PostDto post = postService.recordVideoView(postId, userId, watchedDuration);
        return ResponseEntity.ok(ApiResponse.success("View recorded successfully", post));
    }

    @PostMapping("/{postId}/comments")
    @Operation(summary = "Add a comment to a post")
    public ResponseEntity<ApiResponse> addComment(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Username") String username,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentDto comment = postService.addComment(postId, userId, username, request);
        return new ResponseEntity<>(ApiResponse.success("Comment added successfully", comment), HttpStatus.CREATED);
    }

    @GetMapping("/{postId}/comments")
    @Operation(summary = "Get comments for a post")
    public ResponseEntity<ApiResponse> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CommentDto> comments = postService.getComments(postId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Comments fetched successfully", comments));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<ApiResponse> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader("X-User-Id") Long userId) {
        postService.deleteComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }
}
