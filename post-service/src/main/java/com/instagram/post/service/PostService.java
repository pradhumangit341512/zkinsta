package com.instagram.post.service;

import com.instagram.post.dto.*;
import com.instagram.post.entity.Comment;
import com.instagram.post.entity.Like;
import com.instagram.post.entity.Post;
import com.instagram.post.entity.VideoView;
import com.instagram.post.exception.CustomException;
import com.instagram.post.repository.CommentRepository;
import com.instagram.post.repository.LikeRepository;
import com.instagram.post.repository.PostRepository;
import com.instagram.post.repository.VideoViewRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final VideoViewRepository videoViewRepository;
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

    private void updateTrendingHashtags(List<String> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) return;
        for (String hashtag : hashtags) {
            try {
                webClientBuilder.build()
                        .post()
                        .uri("http://trending-service/api/trending/hashtags/{hashtag}", hashtag)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
            } catch (Exception e) {
                log.warn("Failed to update trending hashtag {}: {}", hashtag, e.getMessage());
            }
        }
    }

    @CircuitBreaker(name = "postService", fallbackMethod = "createPostFallback")
    public PostDto createPost(Long userId, String username, CreatePostRequest request) {
        Post post = Post.builder()
                .userId(userId)
                .caption(request.getCaption())
                .mediaUrl(request.getMediaUrl())
                .mediaType(request.getMediaType())
                .privacy(request.getPrivacy() != null ? request.getPrivacy() : Post.Privacy.PUBLIC)
                .filter(request.getFilter())
                .hashtags(request.getHashtags() != null ? request.getHashtags() : List.of())
                .build();

        Post saved = postRepository.save(post);

        // Update trending hashtags via WebClient
        updateTrendingHashtags(saved.getHashtags());

        return mapToDto(saved, userId, username);
    }

    public PostDto createPostFallback(Long userId, String username, CreatePostRequest request, Throwable t) {
        if (t instanceof CustomException) throw (CustomException) t;
        log.error("Circuit breaker fallback for createPost: {}", t.getMessage());
        throw new CustomException("Service temporarily unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    public PostDto getPost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("Post not found", HttpStatus.NOT_FOUND));
        return mapToDto(post, currentUserId, null);
    }

    public List<PostDto> getPostsByUser(Long userId, Long currentUserId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(post -> mapToDto(post, currentUserId, null))
                .collect(Collectors.toList());
    }

    public Page<PostDto> getFeed(List<Long> followingIds, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByUserIdInAndPrivacyOrderByCreatedAtDesc(followingIds, Post.Privacy.PUBLIC, pageable)
                .map(post -> mapToDto(post, currentUserId, null));
    }

    public Page<PostDto> getPublicFeed(Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByPrivacyOrderByCreatedAtDesc(Post.Privacy.PUBLIC, pageable)
                .map(post -> mapToDto(post, currentUserId, null));
    }

    public PostDto updatePost(Long postId, Long userId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("Post not found", HttpStatus.NOT_FOUND));

        if (!post.getUserId().equals(userId)) {
            throw new CustomException("You are not authorized to update this post", HttpStatus.FORBIDDEN);
        }

        if (request.getCaption() != null) post.setCaption(request.getCaption());
        if (request.getPrivacy() != null) post.setPrivacy(request.getPrivacy());
        if (request.getFilter() != null) post.setFilter(request.getFilter());
        if (request.getHashtags() != null) post.setHashtags(request.getHashtags());

        Post updated = postRepository.save(post);
        return mapToDto(updated, userId, null);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("Post not found", HttpStatus.NOT_FOUND));

        if (!post.getUserId().equals(userId)) {
            throw new CustomException("You are not authorized to delete this post", HttpStatus.FORBIDDEN);
        }

        commentRepository.deleteByPostId(postId);
        likeRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }

    @Transactional
    public PostDto likePost(Long postId, Long userId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("Post not found", HttpStatus.NOT_FOUND));

        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new CustomException("You have already liked this post", HttpStatus.CONFLICT);
        }

        Like like = Like.builder()
                .postId(postId)
                .userId(userId)
                .build();
        likeRepository.save(like);

        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);

        return mapToDto(post, userId, username);
    }

    @Transactional
    public PostDto unlikePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("Post not found", HttpStatus.NOT_FOUND));

        Like like = likeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new CustomException("You have not liked this post", HttpStatus.BAD_REQUEST));

        likeRepository.delete(like);
        post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
        postRepository.save(post);

        return mapToDto(post, userId, null);
    }

    public Page<PostDto> getTrendingPosts(Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findTrendingPosts(pageable)
                .map(post -> mapToDto(post, currentUserId, null));
    }

    public Page<PostDto> getTrendingPostsSince(Long currentUserId, int hours, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return postRepository.findTrendingPostsSince(since, pageable)
                .map(post -> mapToDto(post, currentUserId, null));
    }

    public Page<PostDto> getPostsByHashtag(String hashtag, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByHashtag(hashtag, pageable)
                .map(post -> mapToDto(post, currentUserId, null));
    }

    public Page<PostDto> searchPosts(String query, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.searchPosts(query, pageable)
                .map(post -> mapToDto(post, currentUserId, null));
    }

    @Transactional
    public PostDto recordView(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("Post not found", HttpStatus.NOT_FOUND));

        boolean recentView = videoViewRepository.existsByPostIdAndUserIdAndViewedAtAfter(
                postId, userId, LocalDateTime.now().minusMinutes(5));
        if (!recentView) {
            VideoView view = VideoView.builder()
                    .postId(postId)
                    .userId(userId)
                    .watchedDuration(0)
                    .build();
            videoViewRepository.save(view);
            post.setViewsCount(post.getViewsCount() + 1);
            postRepository.save(post);
        }
        return mapToDto(post, userId, null);
    }

    @Transactional
    public CommentDto addComment(Long postId, Long userId, String username, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException("Post not found", HttpStatus.NOT_FOUND));

        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .text(request.getText())
                .build();

        Comment saved = commentRepository.save(comment);
        CommentDto dto = modelMapper.map(saved, CommentDto.class);
        dto.setUsername(username);
        return dto;
    }

    public Page<CommentDto> getComments(Long postId, int page, int size) {
        if (!postRepository.existsById(postId)) {
            throw new CustomException("Post not found", HttpStatus.NOT_FOUND);
        }
        Pageable pageable = PageRequest.of(page, size);
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable)
                .map(c -> {
                    CommentDto dto = modelMapper.map(c, CommentDto.class);
                    dto.setUsername(fetchUsername(c.getUserId()));
                    return dto;
                });
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException("Comment not found", HttpStatus.NOT_FOUND));
        if (!comment.getUserId().equals(userId)) {
            throw new CustomException("You are not authorized to delete this comment", HttpStatus.FORBIDDEN);
        }
        commentRepository.delete(comment);
    }

    private PostDto mapToDto(Post post, Long currentUserId, String knownUsername) {
        PostDto dto = modelMapper.map(post, PostDto.class);
        // Set username: use known username (from header) or fetch via WebClient
        dto.setUsername(knownUsername != null ? knownUsername : fetchUsername(post.getUserId()));
        dto.setCommentsCount(commentRepository.countByPostId(post.getId()));
        if (currentUserId != null) {
            dto.setLikedByCurrentUser(likeRepository.existsByPostIdAndUserId(post.getId(), currentUserId));
        }
        return dto;
    }
}
