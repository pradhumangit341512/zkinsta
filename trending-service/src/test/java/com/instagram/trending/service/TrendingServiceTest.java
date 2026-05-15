package com.instagram.trending.service;

import com.instagram.trending.dto.TrendingHashtagDto;
import com.instagram.trending.entity.TrendingHashtag;
import com.instagram.trending.repository.TrendingHashtagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrendingServiceTest {

    @Mock private TrendingHashtagRepository trendingHashtagRepository;
    @Mock private ModelMapper modelMapper;
    @InjectMocks private TrendingService trendingService;

    private TrendingHashtag testHashtag;
    private TrendingHashtagDto testHashtagDto;

    @BeforeEach
    void setUp() {
        testHashtag = TrendingHashtag.builder()
                .id(1L).hashtag("trending").postCount(100L).viewCount(5000L)
                .lastUpdated(LocalDateTime.now()).build();
        testHashtagDto = TrendingHashtagDto.builder()
                .id(1L).hashtag("trending").postCount(100L).viewCount(5000L).build();
    }

    @Test
    void getTrendingHashtags_Success() {
        when(trendingHashtagRepository.findTopTrending(PageRequest.of(0, 10)))
                .thenReturn(List.of(testHashtag));
        when(modelMapper.map(testHashtag, TrendingHashtagDto.class)).thenReturn(testHashtagDto);

        List<TrendingHashtagDto> results = trendingService.getTrendingHashtags(10);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("trending", results.get(0).getHashtag());
    }

    @Test
    void updateHashtagCount_Existing() {
        when(trendingHashtagRepository.findByHashtag("trending")).thenReturn(Optional.of(testHashtag));
        when(trendingHashtagRepository.save(any(TrendingHashtag.class))).thenReturn(testHashtag);
        when(modelMapper.map(testHashtag, TrendingHashtagDto.class)).thenReturn(testHashtagDto);

        TrendingHashtagDto result = trendingService.updateHashtagCount("trending");

        assertNotNull(result);
        verify(trendingHashtagRepository).save(any(TrendingHashtag.class));
    }

    @Test
    void updateHashtagCount_New() {
        when(trendingHashtagRepository.findByHashtag("newtag")).thenReturn(Optional.empty());
        TrendingHashtag newTag = TrendingHashtag.builder().id(2L).hashtag("newtag").postCount(1L).viewCount(0L).build();
        TrendingHashtagDto newDto = TrendingHashtagDto.builder().id(2L).hashtag("newtag").postCount(1L).viewCount(0L).build();
        when(trendingHashtagRepository.save(any(TrendingHashtag.class))).thenReturn(newTag);
        when(modelMapper.map(newTag, TrendingHashtagDto.class)).thenReturn(newDto);

        TrendingHashtagDto result = trendingService.updateHashtagCount("newtag");

        assertNotNull(result);
        assertEquals("newtag", result.getHashtag());
    }

    @Test
    void incrementViewCount_Success() {
        when(trendingHashtagRepository.findByHashtag("trending")).thenReturn(Optional.of(testHashtag));
        when(trendingHashtagRepository.save(any(TrendingHashtag.class))).thenReturn(testHashtag);
        when(modelMapper.map(testHashtag, TrendingHashtagDto.class)).thenReturn(testHashtagDto);

        TrendingHashtagDto result = trendingService.incrementViewCount("trending");

        assertNotNull(result);
        verify(trendingHashtagRepository).save(any(TrendingHashtag.class));
    }

    @Test
    void searchHashtags_Success() {
        when(trendingHashtagRepository.findByHashtagContainingIgnoreCase("trend"))
                .thenReturn(List.of(testHashtag));
        when(modelMapper.map(testHashtag, TrendingHashtagDto.class)).thenReturn(testHashtagDto);

        List<TrendingHashtagDto> results = trendingService.searchHashtags("trend");

        assertFalse(results.isEmpty());
    }
}
