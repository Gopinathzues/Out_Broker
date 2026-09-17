package outbroker_backend.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import outbroker_backend.booking.entity.Booking;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByTenantIdOrderByVisitDateTimeDesc(UUID tenantId);

    
    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.property.id = :propertyId
          AND b.visitDateTime = :visitDateTime
          AND b.status = 'CONFIRMED'
        """)
boolean existsConfirmedBooking(
        @Param("propertyId") UUID propertyId,
        @Param("visitDateTime") java.time.LocalDateTime visitDateTime
);
    List<Booking> findByOwnerId(@Param("ownerId") UUID ownerId);
}