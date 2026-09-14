package outbroker_backend.property.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.property.entity.Property;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID>, JpaSpecificationExecutor<Property> {

    List<Property> findByCityAndStatus(String city, PropertyStatus status);

    List<Property> findByStatus(PropertyStatus status);

    List<Property> findByOwnerId(UUID ownerId);

    @Query("SELECT p FROM Property p WHERE p.status = 'AVAILABLE' AND p.expiresAt < :now")
    List<Property> findExpiredProperties(@Param("now") LocalDateTime now);
}