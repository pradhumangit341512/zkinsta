package com.instagram.post.service;

import com.instagram.post.dto.*;
import com.instagram.post.entity.Like;
import com.instagram.post.entity.Post;
import com.instagram.post.exception.CustomException;
import com.instagram.post.repository.LikeRepository;
import com.instagram.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

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
    @Mock private ModelMapper modelMapper;
    @InjectMocks private PostService postService;

    private Post testPost;
    private PostDto testPostDto;

    @BeforeEach
    void setUp() {
        testPost = Post.builder()
                .id(1L).userId(1L).username("johndoe").caption("Test post")
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
    void createPost_Success() {
        CreatePostRequest request = CreatePostRequest.builder()
                .caption("Test post").mediaType(Post.MediaType.IMAGE)
                .mediaUrl("http://image.jpg").hashtags(List.of("test")).build();

        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(modelMapper.map(any(Post.class), eq(PostDto.class))).thenReturn(testPostDto);
        when(likeRepository.existsByPostIdAndUserId(anyLong(), anyLong())).thenReturn(false);

        PostDto result = postService.createPost(1L, "johndoe", request);

        assertNotNull(result);
        assertEquals("Test post", result.getCaption());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void getPost_Success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(modelMapper.map(testPost, PostDto.class)).thenReturn(testPostDto);
        when(likeRepository.existsByPostIdAndUserId(1L, 1L)).thenReturn(false);

        PostDto result = postService.getPost(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getPost_NotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(CustomException.class, () -> postService.getPost(999L, 1L));
    }

    @Test
    void updatePost_Success() {
        UpdatePostRequest request = UpdatePostRequest.builder().caption("Updated caption").build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(modelMapper.map(any(Post.class), eq(PostDto.class))).thenReturn(testPostDto);
        when(likeRepository.existsByPostIdAndUserId(anyLong(), anyLong())).thenReturn(false);

        PostDto result = postService.updatePost(1L, 1L, request);

        assertNotNull(result);
        verify(postRepository).save(any(Post.class));
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
    void likePost_Success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(likeRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(false);
        when(likeRepository.save(any(Like.class))).thenReturn(Like.builder().id(1L).build());
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(modelMapper.map(any(Post.class), eq(PostDto.class))).thenReturn(testPostDto);

        PostDto result = postService.likePost(1L, 2L, "janedoe");

        assertNotNull(result);
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    void likePost_AlreadyLiked() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(likeRepository.existsByPostIdAndUserId(1L, 1L)).thenReturn(true);

        assertThrows(CustomException.class, () -> postService.likePost(1L, 1L, "johndoe"));
    }

    @Test
    void unlikePost_Success() {
        Like like = Like.builder().id(1L).postId(1L).userId(1L).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(likeRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.of(like));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(modelMapper.map(any(Post.class), eq(PostDto.class))).thenReturn(testPostDto);
        when(likeRepository.existsByPostIdAndUserId(1L, 1L)).thenReturn(false);

        PostDto result = postService.unlikePost(1L, 1L);

        assertNotNull(result);
        verify(likeRepository).delete(like);
    }

    @Test
    void unlikePost_NotLiked() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(likeRepository.findByPostIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> postService.unlikePost(1L, 1L));
    }

    @Test
    void getPostsByUser_Success() {
        when(postRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(testPost));
        when(modelMapper.map(testPost, PostDto.class)).thenReturn(testPostDto);
        when(likeRepository.existsByPostIdAndUserId(anyLong(), anyLong())).thenReturn(false);

        List<PostDto> results = postService.getPostsByUser(1L, 1L);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }
}
