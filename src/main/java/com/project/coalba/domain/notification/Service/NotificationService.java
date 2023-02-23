package com.project.coalba.domain.notification.Service;

import com.project.coalba.domain.auth.entity.User;
import com.project.coalba.domain.notification.Repository.NotificationRepository;
import com.project.coalba.domain.notification.entity.Notification;
import com.project.coalba.global.exception.BusinessException;
import com.project.coalba.global.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.project.coalba.global.exception.ErrorCode.NOTIFICATION_NOT_FOUNE;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private final UserUtil userUtil;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void save(String deviceToken) {
        User currentUser = userUtil.getCurrentUser();
        Optional<Notification> notification = notificationRepository.findByUser(currentUser);
        notification.ifPresentOrElse(
                noti -> noti.updateDeviceToken(deviceToken),
                () -> notificationRepository.save(Notification.builder().user(currentUser).deviceToken(deviceToken).build()));
    }

    @Transactional
    public String getDeviceTokenByStaff(Long staffId) {
        Notification notification = notificationRepository.getNotificationByStaff(staffId);
        try {
            return notification.getDeviceToken();
        } catch (Exception e) {
            throw new BusinessException(NOTIFICATION_NOT_FOUNE);
        }
    }

    @Transactional
    public String getDeviceTokenByBoss(Long bossId) {
        Notification notification = notificationRepository.getNotificationByBoss(bossId);
        try {
            return notification.getDeviceToken();
        } catch (Exception e) {
            throw new BusinessException(NOTIFICATION_NOT_FOUNE);
        }
    }
}