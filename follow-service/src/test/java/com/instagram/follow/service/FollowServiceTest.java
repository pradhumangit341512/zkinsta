package com.instagram.follow.service;

import com.instagram.follow.dto.*;
import com.instagram.follow.entity.Follow;
import com.instagram.follow.exception.CustomException;
import com.instagram.follow.repository.FollowRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock private FollowRepository followRepository;
    @Mock private NotificationService notificationService;
    @Mock private ModelMapper modelMapper;
    @Mock private WebClient.Builder webClientBuilder;
    @InjectMocks private FollowService followService;

    private Follow testFollow;
    private FollowDto testFollowDto;

    @BeforeEach
    void setUp() {
        testFollow = Follow.builder()
                .id(1L).followerId(1L)
                .followingId(2L)
                .createdAt(LocalDateTime.now()).build();
        testFollowDto = FollowDto.builder()
                .id(1L).followerId(1L).followerUsername("johndoe")
                .followingId(2L).followingUsername("janedoe").build();
    }

    @Test
    void followUser_SelfFollow() {
        FollowRequest request = FollowRequest.builder().followingId(1L).build();
        assertThrows(CustomException.class, () -> followService.followUser(1L, "johndoe", request));
    }

    @Test
    void followUser_AlreadyFollowing() {
        FollowRequest request = FollowRequest.builder().followingId(2L).build();
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);
        assertThrows(CustomException.class, () -> followService.followUser(1L, "johndoe", request));
    }

    @Test
    void unfollowUser_Success() {
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.of(testFollow));
        followService.unfollowUser(1L, 2L);
        verify(followRepository).delete(testFollow);
    }

    @Test
    void unfollowUser_NotFollowing() {
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.empty());
        assertThrows(CustomException.class, () -> followService.unfollowUser(1L, 2L));
    }

    @Test
    void getFollowCounts_Success() {
        when(followRepository.countByFollowingId(1L)).thenReturn(10L);
        when(followRepository.countByFollowerId(1L)).thenReturn(5L);

        FollowCountDto counts = followService.getFollowCounts(1L);
        assertEquals(10L, counts.getFollowersCount());
        assertEquals(5L, counts.getFollowingCount());
    }

    @Test
    void isFollowing_True() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);
        assertTrue(followService.isFollowing(1L, 2L));
    }

    @Test
    void isFollowing_False() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);
        assertFalse(followService.isFollowing(1L, 2L));
    }

    @Test
    void getFollowingIds_Success() {
        when(followRepository.findByFollowerId(1L)).thenReturn(List.of(testFollow));
        List<Long> ids = followService.getFollowingIds(1L);
        assertEquals(1, ids.size());
        assertEquals(2L, ids.get(0));
    }
}
