package com.instagram.follow.repository;

import com.instagram.follow.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    long countByReceiverIdAndReadFalse(Long receiverId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.receiverId = :receiverId AND n.read = false")
    int markAllAsReadByReceiverId(@Param("receiverId") Long receiverId);

    void deleteByReceiverIdAndId(Long receiverId, Long notificationId);
}
