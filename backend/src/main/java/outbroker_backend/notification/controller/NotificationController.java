package outbroker_backend.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.notification.entity.Notification;
import outbroker_backend.notification.service.NotificationService;
import outbroker_backend.user.entity.User;

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

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get my notifications",
            description = "Retrieves notifications belonging to the authenticated user."
    )
    public ResponseEntity<List<Notification>> getUserNotifications(
            Authentication authentication) {

        UUID userId = extractUserId(authentication);

        return ResponseEntity.ok(
                notificationService.getUserNotifications(userId)
        );
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Mark my notification as read",
            description = "Marks a notification belonging to the authenticated user as read."
    )
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = extractUserId(authentication);

        notificationService.markAsRead(id, userId);

        return ResponseEntity.ok().build();
    }

    private UUID extractUserId(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user.getId();
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Unable to determine authenticated user ID"
            );
        }
    }
}