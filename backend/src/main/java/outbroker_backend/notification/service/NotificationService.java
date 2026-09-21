package outbroker_backend.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.common.exception.UnauthorizedAccessException;
import outbroker_backend.notification.entity.Notification;
import outbroker_backend.notification.repository.NotificationRepository;
import outbroker_backend.user.entity.User;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void sendNotification(
            User user,
            String title,
            String message,
            String type) {

        Notification notification =
                new Notification(
                        user,
                        title,
                        message,
                        type
                );

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(UUID userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markAsRead(
            UUID notificationId,
            UUID userId) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found"
                                ));

        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException(
                    "You are not authorized to update this notification"
            );
        }

        notification.setRead(true);
    }
}