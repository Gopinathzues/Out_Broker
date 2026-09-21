package outbroker_backend.user.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.entity.UserSession;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository
        extends JpaRepository<UserSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s
        FROM UserSession s
        WHERE s.refreshTokenHash = :refreshTokenHash
          AND s.isRevoked = false
        """)
    Optional<UserSession> findByRefreshTokenHashAndIsRevokedFalse(
            @Param("refreshTokenHash") String refreshTokenHash
    );

    @Modifying
    @Query("""
        UPDATE UserSession s
        SET s.isRevoked = true
        WHERE s.user = :user
          AND s.isRevoked = false
        """)
    void revokeAllActiveUserSessions(
            @Param("user") User user
    );
}