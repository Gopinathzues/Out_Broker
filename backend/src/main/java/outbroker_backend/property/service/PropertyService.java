package outbroker_backend.property.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.common.enums.UserRole;
import outbroker_backend.common.enums.VerificationStatus;
import outbroker_backend.property.dto.PropertyResponse;
import outbroker_backend.property.dto.PropertySearchCriteria;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.property.specification.PropertySpecification;
import outbroker_backend.user.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

        // Verify that the user is allowed to modify this property.
        validateOwnershipOrAdmin(property, currentUser);

        property.setTitle(updatedDetails.getTitle());
        property.setDescription(updatedDetails.getDescription());
        property.setMonthlyRent(updatedDetails.getMonthlyRent());
        property.setSecurityDeposit(updatedDetails.getSecurityDeposit());
        property.setPropertyType(updatedDetails.getPropertyType());
        property.setBedrooms(updatedDetails.getBedrooms());
        property.setBathrooms(updatedDetails.getBathrooms());
        property.setCity(updatedDetails.getCity());
        property.setAddress(updatedDetails.getAddress());

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

        // Verify that the user is allowed to delete this property.
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

