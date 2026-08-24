package outbroker_backend.verification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import outbroker_backend.verification.entity.VerificationSubmission;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationSubmissionRepository extends JpaRepository<VerificationSubmission, UUID> {
    
    @Query("SELECT v FROM VerificationSubmission v WHERE v.user.id = :userId ORDER BY v.createdAt DESC")
    Optional<VerificationSubmission> findLatestByUserId(@Param("userId") UUID userId);
}