package outbroker_backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private UUID id;
    private UUID roomId;
    private UUID senderId;
    private UUID recipientId;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
}