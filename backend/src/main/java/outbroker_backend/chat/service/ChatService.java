package outbroker_backend.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.chat.dto.ChatMessageRequest;
import outbroker_backend.chat.dto.ChatMessageResponse;
import outbroker_backend.chat.dto.ChatRoomResponse;
import outbroker_backend.chat.entity.ChatMessage;
import outbroker_backend.chat.entity.ChatRoom;
import outbroker_backend.chat.repository.ChatMessageRepository;
import outbroker_backend.chat.repository.ChatRoomRepository;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.common.exception.UnauthorizedAccessException;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PropertyRepository propertyRepository;

    @Transactional
    public ChatRoomResponse getOrCreateRoom(UUID propertyId, UUID currentUserId) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Property not found with ID: " + propertyId
                        ));

        UUID landlordId = property.getOwner().getId();
        UUID tenantId = currentUserId;

        if (landlordId.equals(currentUserId)) {
            throw new IllegalArgumentException(
                    "Property owners cannot open a tenant chat with themselves"
            );
        }

        ChatRoom room = chatRoomRepository
                .findByPropertyIdAndTenantIdAndLandlordId(
                        propertyId,
                        tenantId,
                        landlordId
                )
                .orElseGet(() ->
                        chatRoomRepository.save(
                                ChatRoom.builder()
                                        .property(property)
                                        .tenantId(tenantId)
                                        .landlordId(landlordId)
                                        .build()
                        )
                );

        return mapToRoomResponse(room, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getUserRooms(UUID userId) {

        return chatRoomRepository.findAllUserChatRooms(userId)
                .stream()
                .map(room -> mapToRoomResponse(room, userId))
                .toList();
    }

    @Transactional
    public ChatMessageResponse saveMessage(
            UUID senderId,
            ChatMessageRequest request
    ) {

        ChatRoom room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Chat room not found"
                        ));

        boolean isTenant = room.getTenantId().equals(senderId);
        boolean isLandlord = room.getLandlordId().equals(senderId);

        if (!isTenant && !isLandlord) {
            throw new UnauthorizedAccessException(
                    "You are not a participant in this chat room"
            );
        }

        UUID expectedRecipientId = isTenant
                ? room.getLandlordId()
                : room.getTenantId();

        if (!expectedRecipientId.equals(request.getRecipientId())) {
            throw new UnauthorizedAccessException(
                    "Recipient is not a participant in this chat room"
            );
        }

        if (request.getContent() == null
                || request.getContent().isBlank()) {

            throw new IllegalArgumentException(
                    "Message content cannot be empty"
            );
        }

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .senderId(senderId)
                .recipientId(expectedRecipientId)
                .content(request.getContent().trim())
                .isRead(false)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        return mapToMessageResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getRoomMessages(
            UUID roomId,
            UUID userId,
            Pageable pageable
    ) {

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Chat room not found"
                        ));

        if (!room.getTenantId().equals(userId)
                && !room.getLandlordId().equals(userId)) {

            throw new UnauthorizedAccessException(
                    "You are not authorized to view these messages"
            );
        }

        return chatMessageRepository
                .findByChatRoomIdOrderByCreatedAtDesc(
                        roomId,
                        pageable
                )
                .map(this::mapToMessageResponse);
    }

    @Transactional
    public void markAsRead(UUID roomId, UUID userId) {

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Chat room not found"
                        ));

        if (!room.getTenantId().equals(userId)
                && !room.getLandlordId().equals(userId)) {

            throw new UnauthorizedAccessException(
                    "You are not authorized to access this chat room"
            );
        }

        chatMessageRepository.markMessagesAsRead(
                roomId,
                userId
        );
    }

    private ChatRoomResponse mapToRoomResponse(
            ChatRoom room,
            UUID userId
    ) {

        Long unread =
                chatMessageRepository
                        .countByChatRoomIdAndRecipientIdAndIsReadFalse(
                                room.getId(),
                                userId
                        );

        return ChatRoomResponse.builder()
                .id(room.getId())
                .propertyId(room.getProperty().getId())
                .propertyTitle(room.getProperty().getTitle())
                .tenantId(room.getTenantId())
                .landlordId(room.getLandlordId())
                .createdAt(room.getCreatedAt())
                .unreadCount(unread)
                .build();
    }

    private ChatMessageResponse mapToMessageResponse(
            ChatMessage msg
    ) {

        return ChatMessageResponse.builder()
                .id(msg.getId())
                .roomId(msg.getChatRoom().getId())
                .senderId(msg.getSenderId())
                .recipientId(msg.getRecipientId())
                .content(msg.getContent())
                .isRead(msg.isRead())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}