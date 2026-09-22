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
import java.util.Optional;
import java.util.UUID;
@Repository
public interface PropertyRepository
        extends JpaRepository<Property, UUID>, JpaSpecificationExecutor<Property> {

    @Query("""
            SELECT p
            FROM Property p
            WHERE LOWER(p.city) = LOWER(:city)
              AND p.status = :status
              AND (p.expiresAt IS NULL OR p.expiresAt > :now)
            """)
    List<Property> findByCityAndStatusAndNotExpired(
            @Param("city") String city,
            @Param("status") PropertyStatus status,
            @Param("now") LocalDateTime now
    );

    @Query("""
            SELECT p
            FROM Property p
            WHERE p.status = :status
              AND (p.expiresAt IS NULL OR p.expiresAt > :now)
            """)
    List<Property> findAvailableProperties(
            @Param("status") PropertyStatus status,
            @Param("now") LocalDateTime now
    );

    @Query("""
            SELECT p
            FROM Property p
            WHERE p.id = :id
              AND p.status = :status
              AND (p.expiresAt IS NULL OR p.expiresAt > :now)
            """)
    Optional<Property> findPublicPropertyById(
            @Param("id") UUID id,
            @Param("status") PropertyStatus status,
            @Param("now") LocalDateTime now
    );

    List<Property> findByOwnerId(UUID ownerId);

    @Query("""
            SELECT p
            FROM Property p
            WHERE p.status = 'AVAILABLE'
              AND p.expiresAt IS NOT NULL
              AND p.expiresAt < :now
            """)
    List<Property> findExpiredProperties(
            @Param("now") LocalDateTime now
    );
}