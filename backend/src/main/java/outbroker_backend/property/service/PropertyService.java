package outbroker_backend.property.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.common.enums.TenantPreference;
import outbroker_backend.common.enums.TransactionType;
import outbroker_backend.common.enums.UserRole;
import outbroker_backend.common.enums.VerificationStatus;
import outbroker_backend.property.dto.PropertyResponse;
import outbroker_backend.property.dto.PropertySearchCriteria;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.property.specification.PropertySpecification;
import outbroker_backend.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    // =========================================================
    // CREATE PROPERTY
    // =========================================================

    @Transactional
    public Property createProperty(Property property, User currentUser) {

        // ADMIN can create properties without landlord verification.
        if (currentUser.getRole() != UserRole.ADMIN) {

            // Only LANDLORD can create property listings.
            if (currentUser.getRole() != UserRole.LANDLORD) {
                throw new AccessDeniedException(
                        "Only landlords or admins can create property listings."
                );
            }

            // Landlord must be FULLY_VERIFIED.
            if (currentUser.getVerificationStatus() != VerificationStatus.FULLY_VERIFIED) {
                throw new AccessDeniedException(
                        "Only fully verified landlords are allowed to create property listings."
                );
            }
        }

        // Always assign the authenticated user as the property owner.
        property.setOwner(currentUser);

        // Set default lifecycle timestamps and status defaults
        if (property.getTransactionType() == null) {
            property.setTransactionType(TransactionType.RENT);
        }
        if (property.getTenantPreference() == null) {
            property.setTenantPreference(TenantPreference.ANY);
        }
        if (property.getStatus() == null) {
            property.setStatus(PropertyStatus.AVAILABLE);
        }
        property.setLastRefreshedAt(LocalDateTime.now());

        return propertyRepository.save(property);
    }

    // =========================================================
    // PUBLIC PROPERTY DISCOVERY
    // =========================================================

    @Transactional(readOnly = true)
    public List<Property> getAvailablePropertiesByCity(String city) {

        return propertyRepository.findByCityAndStatus(
                city,
                PropertyStatus.AVAILABLE
        );
    }

    @Transactional(readOnly = true)
    public List<Property> getPropertiesByOwner(UUID ownerId) {
        return propertyRepository.findByOwnerId(ownerId);
    }

    // =========================================================
    // PROPERTY SEARCH / FILTER
    // =========================================================

    @Transactional(readOnly = true)
    public List<PropertyResponse> searchPropertiesWithFilters(
            PropertySearchCriteria criteria) {

        return propertyRepository
                .findAll(PropertySpecification.buildSpecification(criteria))
                .stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }
    // Add this method to your PropertyService.java

public Page<Property> searchProperties(PropertySearchCriteria criteria, Pageable pageable) {
    Specification<Property> spec = PropertySpecification.buildSpecification(criteria);
    return propertyRepository.findAll(spec, pageable);
}

    // =========================================================
    // NEARBY PROPERTIES
    // =========================================================

    @Transactional(readOnly = true)
    public List<PropertyResponse> findNearbyProperties(
            double latitude,
            double longitude,
            double radiusKm) {

        return propertyRepository
                .findByStatus(PropertyStatus.AVAILABLE)
                .stream()
                .filter(property -> {

                    Double propertyLatitude = property.getLatitude();
                    Double propertyLongitude = property.getLongitude();

                    if (propertyLatitude == null || propertyLongitude == null) {
                        return false;
                    }

                    return calculateDistanceKm(
                            latitude,
                            longitude,
                            propertyLatitude,
                            propertyLongitude
                    ) <= radiusKm;
                })
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    private double calculateDistanceKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(latDistance / 2)
                        * Math.sin(latDistance / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2)
                        * Math.sin(lonDistance / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return EARTH_RADIUS_KM * c;
    }

    // =========================================================
    // GET PROPERTY
    // =========================================================

    @Transactional(readOnly = true)
    public Property getPropertyById(UUID id) {

        return propertyRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Property not found with ID: " + id
                        )
                );
    }

    // =========================================================
    // UPDATE PROPERTY
    // =========================================================

    @Transactional
    public Property updateProperty(
            UUID propertyId,
            Property updatedDetails,
            User currentUser) {

        Property property = getPropertyById(propertyId);

        // Verify ownership/RBAC permissions
        validateOwnershipOrAdmin(property, currentUser);

        // Core information
        property.setTitle(updatedDetails.getTitle());
        property.setDescription(updatedDetails.getDescription());
        property.setMonthlyRent(updatedDetails.getMonthlyRent());
        property.setSecurityDeposit(updatedDetails.getSecurityDeposit());
        property.setMaintenanceFee(updatedDetails.getMaintenanceFee());

        // Domain specifications
        property.setPropertyType(updatedDetails.getPropertyType());
        property.setTransactionType(updatedDetails.getTransactionType());
        property.setFurnishingStatus(updatedDetails.getFurnishingStatus());
        property.setTenantPreference(updatedDetails.getTenantPreference());

        // Physical parameters
        property.setBedrooms(updatedDetails.getBedrooms());
        property.setBathrooms(updatedDetails.getBathrooms());
        property.setPropertyAgeYears(updatedDetails.getPropertyAgeYears());
        property.setFloorNumber(updatedDetails.getFloorNumber());
        property.setTotalFloors(updatedDetails.getTotalFloors());
        property.setFacingDirection(updatedDetails.getFacingDirection());
        property.setParkingSpaces(updatedDetails.getParkingSpaces());

        // Location & Availability
        property.setCity(updatedDetails.getCity());
        property.setAddress(updatedDetails.getAddress());
        property.setLandmark(updatedDetails.getLandmark());
        property.setLatitude(updatedDetails.getLatitude());
        property.setLongitude(updatedDetails.getLongitude());
        property.setAvailabilityDate(updatedDetails.getAvailabilityDate());

        // Hostel / PG specifications
        property.setHostelGenderPreference(updatedDetails.getHostelGenderPreference());
        property.setFoodAvailability(updatedDetails.getFoodAvailability());
        property.setIsAc(updatedDetails.getIsAc());
        property.setCurfewTime(updatedDetails.getCurfewTime());
        property.setAllowedStayDuration(updatedDetails.getAllowedStayDuration());

        // Amenities & Lifecycle refresh
        if (updatedDetails.getAmenities() != null) {
            property.setAmenities(updatedDetails.getAmenities());
        }
        property.setLastRefreshedAt(LocalDateTime.now());

        return propertyRepository.save(property);
    }

    // =========================================================
    // DELETE PROPERTY
    // =========================================================

    @Transactional
    public void deleteProperty(
            UUID propertyId,
            User currentUser) {

        Property property = getPropertyById(propertyId);

        // Verify ownership/RBAC permissions
        validateOwnershipOrAdmin(property, currentUser);

        propertyRepository.delete(property);
    }

    // =========================================================
    // AUTHORIZATION
    // =========================================================

    private void validateOwnershipOrAdmin(
            Property property,
            User currentUser) {

        // ADMIN bypasses ownership and verification checks.
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        // Only LANDLORD can modify/delete property listings.
        if (currentUser.getRole() != UserRole.LANDLORD) {
            throw new AccessDeniedException(
                    "Only landlords or admins can modify or delete properties."
            );
        }

        // Landlord must be FULLY_VERIFIED.
        if (currentUser.getVerificationStatus()
                != VerificationStatus.FULLY_VERIFIED) {

            throw new AccessDeniedException(
                    "Landlord must be fully verified to modify or delete properties."
            );
        }

        // Prevent cross-landlord attacks.
        if (property.getOwner() == null
                || !property.getOwner().getId().equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "You do not have permission to modify or delete this property."
            );
        }
    }
}