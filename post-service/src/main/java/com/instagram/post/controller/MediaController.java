package com.instagram.post.controller;

import com.instagram.post.dto.ApiResponse;
import com.instagram.post.entity.MediaFile;
import com.instagram.post.exception.CustomException;
import com.instagram.post.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/quicktime", "video/webm"
    );

    private final MediaFileRepository mediaFileRepository;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadMedia(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new CustomException("File is empty", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new CustomException("File type not allowed. Use JPEG, PNG, GIF, WebP, MP4, MOV, or WebM.", HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new CustomException("File size exceeds 10MB limit", HttpStatus.BAD_REQUEST);
        }

        try {
            MediaFile mediaFile = MediaFile.builder()
                    .contentType(contentType)
                    .filename(file.getOriginalFilename())
                    .data(file.getBytes())
                    .userId(userId)
                    .build();

            MediaFile saved = mediaFileRepository.save(mediaFile);

            String mediaUrl = "/api/media/" + saved.getId();
            log.info("User {} uploaded media: id={}", userId, saved.getId());

            return new ResponseEntity<>(
                    ApiResponse.success("Media uploaded successfully", Map.of("mediaUrl", mediaUrl)),
                    HttpStatus.CREATED
            );
        } catch (IOException e) {
            log.error("Failed to upload media", e);
            throw new CustomException("Failed to upload media", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getMedia(@PathVariable Long id) {
        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> new CustomException("Media not found", HttpStatus.NOT_FOUND));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mediaFile.getContentType()))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(mediaFile.getData());
    }
}
