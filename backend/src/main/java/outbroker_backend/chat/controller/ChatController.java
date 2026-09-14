package outbroker_backend.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.chat.dto.ChatMessageResponse;
import outbroker_backend.chat.dto.ChatRoomResponse;
import outbroker_backend.chat.service.ChatService;
import outbroker_backend.common.exception.UnauthorizedAccessException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(
        name = "Chat",
        description = "Property chat rooms and messaging APIs"
)
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms/property/{propertyId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get or create property chat room",
            description = "Gets an existing chat room for a property or creates one for the authenticated user."
    )
    public ResponseEntity<ChatRoomResponse> getOrCreateRoom(
            Authentication authentication,
            @PathVariable UUID propertyId) {

        UUID userId = extractUserId(authentication);
        return ResponseEntity.ok(
                chatService.getOrCreateRoom(propertyId, userId)
        );
    }

    @GetMapping("/rooms")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get user chat rooms",
            description = "Retrieves all chat rooms available to the authenticated user."
    )
    public ResponseEntity<List<ChatRoomResponse>> getUserRooms(
            Authentication authentication) {

        UUID userId = extractUserId(authentication);
        return ResponseEntity.ok(
                chatService.getUserRooms(userId)
        );
    }

    @GetMapping("/rooms/{roomId}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get room messages",
            description = "Retrieves paginated messages from a specific chat room."
    )
    public ResponseEntity<Page<ChatMessageResponse>> getRoomMessages(
            Authentication authentication,
            @PathVariable UUID roomId,
            @PageableDefault(size = 20) Pageable pageable) {

        UUID userId = extractUserId(authentication);
        return ResponseEntity.ok(
                chatService.getRoomMessages(roomId, userId, pageable)
        );
    }

    @PatchMapping("/rooms/{roomId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Mark chat room as read",
            description = "Marks messages in a chat room as read for the authenticated user."
    )
    public ResponseEntity<Void> markAsRead(
            Authentication authentication,
            @PathVariable UUID roomId) {

        UUID userId = extractUserId(authentication);
        chatService.markAsRead(roomId, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedAccessException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UUID uuid) return uuid;

        if (principal instanceof String str) {
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException ignored) {
            }
        }

        try {
            var method = principal.getClass().getMethod("getId");
            Object id = method.invoke(principal);

            if (id instanceof UUID uuid) return uuid;
            if (id instanceof String str) return UUID.fromString(str);

        } catch (Exception ignored) {
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (Exception e) {
            throw new UnauthorizedAccessException(
                    "Could not resolve authenticated user ID"
            );
        }
    }
}