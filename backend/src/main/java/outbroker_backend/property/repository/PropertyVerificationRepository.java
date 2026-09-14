package outbroker_backend.property.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import outbroker_backend.common.enums.VerificationStatus;
import outbroker_backend.property.entity.PropertyVerification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyVerificationRepository extends JpaRepository<PropertyVerification, UUID> {
    Optional<PropertyVerification> findByPropertyId(UUID propertyId);
    List<PropertyVerification> findByVerificationStatus(VerificationStatus verificationStatus);
}