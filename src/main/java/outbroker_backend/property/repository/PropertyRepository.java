package outbroker_backend.property.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.property.entity.Property;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID>, JpaSpecificationExecutor<Property> {

    // Derived query method to support getAvailablePropertiesByCity
    List<Property> findByCityAndStatus(String city, PropertyStatus status);

    List<Property> findByStatus(PropertyStatus status);
}