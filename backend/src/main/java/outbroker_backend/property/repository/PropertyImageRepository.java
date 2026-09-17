package outbroker_backend.property.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import outbroker_backend.property.entity.PropertyImage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, UUID> {

    List<PropertyImage> findByPropertyId(UUID propertyId);

    Optional<PropertyImage> findByIdAndPropertyId(UUID imageId, UUID propertyId);
}