package outbroker_backend.property.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.property.dto.PropertyResponse;
import outbroker_backend.property.dto.PropertySearchCriteria;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.property.specification.PropertySpecification;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public List<Property> getAvailablePropertiesByCity(String city) {
        return propertyRepository.findByCityIgnoreCaseAndStatus(city, PropertyStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public List<Property> getPropertiesByOwner(UUID ownerId) {
        return propertyRepository.findByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public Property getPropertyById(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> searchPropertiesWithFilters(PropertySearchCriteria criteria) {
        return propertyRepository.findAll(PropertySpecification.buildSpecification(criteria))
                .stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> findNearbyProperties(double latitude, double longitude, double radiusKm) {
        return propertyRepository.findByStatus(PropertyStatus.AVAILABLE)
                .stream()
                .filter(property -> {
                    Double propLat = property.getLatitude();
                    Double propLng = property.getLongitude();
                    if (propLat == null || propLng == null) {
                        return false;
                    }
                    return calculateDistanceKm(latitude, longitude, propLat, propLng) <= radiusKm;
                })
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Transactional
    public Property updateProperty(UUID id, Property updatedData) {
        Property existing = getPropertyById(id);
        existing.setTitle(updatedData.getTitle());
        existing.setDescription(updatedData.getDescription());
        existing.setMonthlyRent(updatedData.getMonthlyRent());
        existing.setSecurityDeposit(updatedData.getSecurityDeposit());
        existing.setPropertyType(updatedData.getPropertyType());
        existing.setBedrooms(updatedData.getBedrooms());
        existing.setBathrooms(updatedData.getBathrooms());
        existing.setAddress(updatedData.getAddress());
        existing.setCity(updatedData.getCity());
        return propertyRepository.save(existing);
    }

    @Transactional
    public void deleteProperty(UUID id) {
        Property property = getPropertyById(id);
        propertyRepository.delete(property);
    }
}