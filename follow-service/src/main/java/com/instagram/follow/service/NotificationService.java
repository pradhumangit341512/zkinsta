package com.instagram.follow.service;

import com.instagram.follow.dto.CreateNotificationRequest;
import com.instagram.follow.dto.NotificationDto;
import com.instagram.follow.entity.Notification;
import com.instagram.follow.exception.CustomException;
import com.instagram.follow.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;

    public NotificationDto createNotification(CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .senderId(request.getSenderId())
                .senderUsername(request.getSenderUsername())
                .receiverId(request.getReceiverId())
                .type(Notification.NotificationType.valueOf(request.getType()))
                .message(request.getMessage())
                .referenceId(request.getReferenceId())
                .build();

        Notification saved = notificationRepository.save(notification);
        return modelMapper.map(saved, NotificationDto.class);
    }

    public Page<NotificationDto> getNotifications(Long userId, int page, int size) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(n -> modelMapper.map(n, NotificationDto.class));
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
