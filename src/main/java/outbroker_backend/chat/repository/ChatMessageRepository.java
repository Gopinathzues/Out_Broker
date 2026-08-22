package outbroker_backend.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import outbroker_backend.chat.entity.ChatMessage;

import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(UUID roomId, Pageable pageable);

    Long countByChatRoomIdAndRecipientIdAndIsReadFalse(UUID roomId, UUID recipientId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.chatRoom.id = :roomId AND m.recipientId = :recipientId AND m.isRead = false")
    void markMessagesAsRead(@Param("roomId") UUID roomId, @Param("recipientId") UUID recipientId);
}