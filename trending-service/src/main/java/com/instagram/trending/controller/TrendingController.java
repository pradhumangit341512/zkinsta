package com.instagram.trending.controller;

import com.instagram.trending.dto.ApiResponse;
import com.instagram.trending.dto.TrendingHashtagDto;
import com.instagram.trending.service.TrendingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trending")
@RequiredArgsConstructor
@Tag(name = "Trending", description = "Trending content APIs")
public class TrendingController {

    private final TrendingService trendingService;

    @GetMapping("/hashtags")
    @Operation(summary = "Get trending hashtags")
    public ResponseEntity<ApiResponse> getTrendingHashtags(
            @RequestParam(defaultValue = "20") int limit) {
        List<TrendingHashtagDto> hashtags = trendingService.getTrendingHashtags(limit);
        return ResponseEntity.ok(ApiResponse.success("Trending hashtags fetched successfully", hashtags));
    }

    @PostMapping("/hashtags/{hashtag}")
    @Operation(summary = "Update hashtag count when a post uses it")
    public ResponseEntity<ApiResponse> updateHashtagCount(@PathVariable String hashtag) {
        TrendingHashtagDto result = trendingService.updateHashtagCount(hashtag);
        return ResponseEntity.ok(ApiResponse.success("Hashtag count updated", result));
    }

    @PostMapping("/hashtags/{hashtag}/view")
    @Operation(summary = "Increment view count for a hashtag")
    public ResponseEntity<ApiResponse> incrementViewCount(@PathVariable String hashtag) {
        TrendingHashtagDto result = trendingService.incrementViewCount(hashtag);
        return ResponseEntity.ok(ApiResponse.success("View count updated", result));
    }

    @GetMapping("/hashtags/search")
    @Operation(summary = "Search hashtags")
    public ResponseEntity<ApiResponse> searchHashtags(@RequestParam String query) {
        List<TrendingHashtagDto> hashtags = trendingService.searchHashtags(query);
        return ResponseEntity.ok(ApiResponse.success("Hashtags fetched successfully", hashtags));
    }
}
