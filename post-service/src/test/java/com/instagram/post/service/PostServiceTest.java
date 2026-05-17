package com.instagram.post.service;

import com.instagram.post.dto.*;
import com.instagram.post.entity.Like;
import com.instagram.post.entity.Post;
import com.instagram.post.exception.CustomException;
import com.instagram.post.repository.CommentRepository;
import com.instagram.post.repository.LikeRepository;
import com.instagram.post.repository.PostRepository;
import com.instagram.post.repository.VideoViewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private LikeRepository likeRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private VideoViewRepository videoViewRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private WebClient.Builder webClientBuilder;
    @InjectMocks private PostService postService;

    private Post testPost;
    private PostDto testPostDto;

    @BeforeEach
    void setUp() {
        testPost = Post.builder()
                .id(1L).userId(1L).caption("Test post")
                .mediaType(Post.MediaType.IMAGE).mediaUrl("http://image.jpg")
                .privacy(Post.Privacy.PUBLIC).likesCount(0L).viewsCount(0L)
                .hashtags(List.of("test")).createdAt(LocalDateTime.now()).build();

        testPostDto = PostDto.builder()
                .id(1L).userId(1L).username("johndoe").caption("Test post")
                .mediaType(Post.MediaType.IMAGE).mediaUrl("http://image.jpg")
                .privacy(Post.Privacy.PUBLIC).likesCount(0L).viewsCount(0L)
                .hashtags(List.of("test")).build();
    }

    @Test
    void getPost_NotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(CustomException.class, () -> postService.getPost(999L, 1L));
    }

    @Test
    void updatePost_Unauthorized() {
        UpdatePostRequest request = UpdatePostRequest.builder().caption("Updated").build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        assertThrows(CustomException.class, () -> postService.updatePost(1L, 999L, request));
    }

    @Test
    void deletePost_Success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        postService.deletePost(1L, 1L);
        verify(likeRepository).deleteByPostId(1L);
        verify(postRepository).delete(testPost);
    }

    @Test
    void deletePost_Unauthorized() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        assertThrows(CustomException.class, () -> postService.deletePost(1L, 999L));
    }

    @Test
    void likePost_AlreadyLiked() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(likeRepository.existsByPostIdAndUserId(1L, 1L)).thenReturn(true);
        assertThrows(CustomException.class, () -> postService.likePost(1L, 1L, "johndoe"));
    }

    @Test
    void unlikePost_NotLiked() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(likeRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(CustomException.class, () -> postService.unlikePost(1L, 1L));
    }
}
