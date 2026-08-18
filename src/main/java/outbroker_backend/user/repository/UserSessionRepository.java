package outbroker_backend.user.repository;

import outbroker_backend.user.entity.User;
import outbroker_backend.user.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshTokenHashAndIsRevokedFalse(String refreshTokenHash);

    @Modifying
    @Query("UPDATE UserSession s SET s.isRevoked = true WHERE s.user = :user AND s.isRevoked = false")
    void revokeAllActiveUserSessions(User user);
}