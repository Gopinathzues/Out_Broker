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
public class ChatRoomResponse {
    private UUID id;
    private UUID propertyId;
    private String propertyTitle;
    private UUID tenantId;
    private UUID landlordId;
    private LocalDateTime createdAt;
    private Long unreadCount;
}