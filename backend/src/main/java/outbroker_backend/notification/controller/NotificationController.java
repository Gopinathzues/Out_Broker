package outbroker_backend.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.notification.entity.Notification;
import outbroker_backend.notification.service.NotificationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(
        name = "Notifications",
        description = "User notification retrieval and read-status APIs"
)
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get user notifications",
            description = "Retrieves notifications associated with a user."
    )
    public ResponseEntity<List<Notification>> getUserNotifications(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                notificationService.getUserNotifications(userId)
        );
    }

    @PatchMapping("/{id}/read")
    @Operation(
            summary = "Mark notification as read",
            description = "Marks a notification as read for the specified user."
    )
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID id,
            @RequestParam UUID userId) {

        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }
}