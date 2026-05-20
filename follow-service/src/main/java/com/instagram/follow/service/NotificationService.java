package com.instagram.follow.service;

import com.instagram.follow.dto.CreateNotificationRequest;
import com.instagram.follow.dto.NotificationDto;
import com.instagram.follow.entity.Notification;
import com.instagram.follow.exception.CustomException;
import com.instagram.follow.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;
    private final WebClient.Builder webClientBuilder;

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private static String sanitizeLogInput(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\\r\\n\\t]", "_");
    }

    private String fetchUsername(Long userId) {
        try {
            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri("http://authentication-service/api/auth/users/{userId}", userId)
                    .retrieve()
                    .bodyToMono(MAP_TYPE)
                    .block();
            if (response != null && response.get("data") instanceof Map<?, ?> data) {
                Object username = data.get("username");
                if (username instanceof String usernameStr) {
                    return usernameStr;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch username for userId {}: {}", userId, sanitizeLogInput(e.getMessage()));
        }
        return "user_" + userId;
    }

    public NotificationDto createNotification(CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .type(Notification.NotificationType.valueOf(request.getType()))
                .message(request.getMessage())
                .referenceId(request.getReferenceId())
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationDto dto = modelMapper.map(saved, NotificationDto.class);
        dto.setSenderUsername(fetchUsername(saved.getSenderId()));
        return dto;
    }

    public Page<NotificationDto> getNotifications(Long userId, int page, int size) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId, PageRequest.of(page, Math.min(size, 100)))
                .map(n -> {
                    NotificationDto dto = modelMapper.map(n, NotificationDto.class);
                    dto.setSenderUsername(fetchUsername(n.getSenderId()));
                    return dto;
                });
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByReceiverIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException("Notification not found", HttpStatus.NOT_FOUND));
        if (!notification.getReceiverId().equals(userId)) {
            throw new CustomException("Unauthorized", HttpStatus.FORBIDDEN);
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByReceiverId(userId);
    }
}
