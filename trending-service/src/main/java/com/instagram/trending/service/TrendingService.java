package com.instagram.trending.service;

import com.instagram.trending.dto.TrendingHashtagDto;
import com.instagram.trending.entity.TrendingHashtag;
import com.instagram.trending.repository.TrendingHashtagRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrendingService {

    private final TrendingHashtagRepository trendingHashtagRepository;
    private final ModelMapper modelMapper;

    @CircuitBreaker(name = "trendingService", fallbackMethod = "getTrendingHashtagsFallback")
    public List<TrendingHashtagDto> getTrendingHashtags(int limit) {
        return trendingHashtagRepository.findTopTrending(PageRequest.of(0, limit)).stream()
                .map(h -> modelMapper.map(h, TrendingHashtagDto.class))
                .collect(Collectors.toList());
    }

    public List<TrendingHashtagDto> getTrendingHashtagsFallback(int limit, Throwable t) {
        log.error("Circuit breaker fallback for getTrendingHashtags: {}", t.getMessage());
        return List.of();
    }

    public TrendingHashtagDto updateHashtagCount(String hashtag) {
        TrendingHashtag trending = trendingHashtagRepository.findByHashtag(hashtag.toLowerCase())
                .orElse(TrendingHashtag.builder().hashtag(hashtag.toLowerCase()).postCount(0L).viewCount(0L).build());
        trending.setPostCount(trending.getPostCount() + 1);
        TrendingHashtag saved = trendingHashtagRepository.save(trending);
        return modelMapper.map(saved, TrendingHashtagDto.class);
    }

    public TrendingHashtagDto incrementViewCount(String hashtag) {
        TrendingHashtag trending = trendingHashtagRepository.findByHashtag(hashtag.toLowerCase())
                .orElse(TrendingHashtag.builder().hashtag(hashtag.toLowerCase()).postCount(0L).viewCount(0L).build());
        trending.setViewCount(trending.getViewCount() + 1);
        TrendingHashtag saved = trendingHashtagRepository.save(trending);
        return modelMapper.map(saved, TrendingHashtagDto.class);
    }

    public List<TrendingHashtagDto> searchHashtags(String query) {
        return trendingHashtagRepository.findByHashtagContainingIgnoreCase(query).stream()
                .map(h -> modelMapper.map(h, TrendingHashtagDto.class))
                .collect(Collectors.toList());
    }
}
