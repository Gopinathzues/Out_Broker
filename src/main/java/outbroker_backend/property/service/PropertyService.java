package outbroker_backend.property.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;

import java.util.List;
import java.util.UUID;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Transactional
    public Property createProperty(Property property) {
        return propertyRepository.save(property);
    }

    public List<Property> getAvailablePropertiesByCity(String city) {
        return propertyRepository.findByCityIgnoreCaseAndStatus(city, PropertyStatus.AVAILABLE);
    }

    public List<Property> getPropertiesByOwner(UUID ownerId) {
        return propertyRepository.findByOwnerId(ownerId);
    }
}