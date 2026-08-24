package outbroker_backend.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import outbroker_backend.chat.entity.ChatRoom;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    Optional<ChatRoom> findByPropertyIdAndTenantIdAndLandlordId(UUID propertyId, UUID tenantId, UUID landlordId);

    @Query("SELECT c FROM ChatRoom c WHERE c.tenantId = :userId OR c.landlordId = :userId ORDER BY c.createdAt DESC")
    List<ChatRoom> findAllUserChatRooms(@Param("userId") UUID userId);
}