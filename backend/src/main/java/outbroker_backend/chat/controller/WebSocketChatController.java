package outbroker_backend.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import outbroker_backend.chat.dto.ChatMessageRequest;
import outbroker_backend.chat.dto.ChatMessageResponse;
import outbroker_backend.chat.service.ChatService;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Tag(
        name = "WebSocket Chat",
        description = "Real-time WebSocket chat messaging"
)
public class WebSocketChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    @Operation(
            summary = "Send a chat message",
            description = "Sends a real-time chat message and publishes it to the room and recipient."
    )
    public void sendMessage(
            @Payload ChatMessageRequest request,
            Authentication authentication) {

        UUID senderId = UUID.fromString(authentication.getName());

        ChatMessageResponse response =
                chatService.saveMessage(senderId, request);

        // Publish to room topic
        messagingTemplate.convertAndSend(
                "/topic/room." + request.getRoomId(),
                response
        );

        // Publish directly to recipient's private user queue
        messagingTemplate.convertAndSendToUser(
                request.getRecipientId().toString(),
                "/queue/messages",
                response
        );
    }
}