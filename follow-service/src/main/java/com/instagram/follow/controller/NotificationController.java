package com.instagram.follow.controller;

import com.instagram.follow.dto.ApiResponse;
import com.instagram.follow.dto.CreateNotificationRequest;
import com.instagram.follow.dto.NotificationDto;
import com.instagram.follow.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create a notification")
    public ResponseEntity<ApiResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationDto notification = notificationService.createNotification(request);
        return new ResponseEntity<>(ApiResponse.success("Notification created", notification), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get notifications for current user")
    public ResponseEntity<ApiResponse> getNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<NotificationDto> notifications = notificationService.getNotifications(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched", notifications));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread count fetched", count));
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse> markAsRead(
            @PathVariable Long notificationId,
            @RequestHeader("X-User-Id") Long userId) {
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse> markAllAsRead(
            @RequestHeader("X-User-Id") Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
