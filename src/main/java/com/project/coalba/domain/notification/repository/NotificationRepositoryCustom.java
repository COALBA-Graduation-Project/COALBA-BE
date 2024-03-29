package com.project.coalba.domain.notification.repository;

import com.project.coalba.domain.notification.entity.Notification;

import java.util.Optional;

public interface NotificationRepositoryCustom {
    Optional<Notification> getNotificationByStaff(Long staffId);
    Optional<Notification> getNotificationByBoss(Long bossId);
}
