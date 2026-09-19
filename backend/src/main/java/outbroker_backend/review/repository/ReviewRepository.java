package outbroker_backend.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import outbroker_backend.review.entity.Review;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByTenantIdAndPropertyId(
            UUID tenantId,
            UUID propertyId
    );

    Optional<Review> findByTenantIdAndPropertyId(
            UUID tenantId,
            UUID propertyId
    );

    Page<Review> findByPropertyId(
            UUID propertyId,
            Pageable pageable
    );

    @Query("""
        SELECT AVG(r.rating)
        FROM Review r
        WHERE r.property.id = :propertyId
        """)
    Double getAverageRatingByPropertyId(
            @Param("propertyId") UUID propertyId
    );

    Long countByPropertyId(UUID propertyId);
}