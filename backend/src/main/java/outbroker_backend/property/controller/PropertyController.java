package outbroker_backend.property.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.common.dto.ApiResponse;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.property.dto.CreatePropertyRequest;
import outbroker_backend.property.dto.PropertyResponse;
import outbroker_backend.property.dto.PropertySearchCriteria;
import outbroker_backend.property.dto.UpdatePropertyRequest;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.service.PropertyService;
import outbroker_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @Valid @RequestBody CreatePropertyRequest request,
            @AuthenticationPrincipal User currentUser) {

        Property property = new Property();

        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setMonthlyRent(request.getMonthlyRent());
        property.setSecurityDeposit(request.getSecurityDeposit());
        property.setMaintenanceFee(request.getMaintenanceFee());

        property.setPropertyType(request.getPropertyType());
        property.setTransactionType(request.getTransactionType());
        property.setFurnishingStatus(request.getFurnishingStatus());
        property.setTenantPreference(request.getTenantPreference());

        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());
        property.setPropertyAgeYears(request.getPropertyAgeYears());
        property.setFloorNumber(request.getFloorNumber());
        property.setTotalFloors(request.getTotalFloors());
        property.setFacingDirection(request.getFacingDirection());
        property.setParkingSpaces(request.getParkingSpaces());

        property.setLatitude(request.getLatitude());
        property.setLongitude(request.getLongitude());
        property.setCity(request.getCity());
        property.setAddress(request.getAddress());
        property.setLandmark(request.getLandmark());

        property.setAvailabilityDate(request.getAvailabilityDate());
        property.setAmenities(request.getAmenities());

        property.setOwner(currentUser);

        Property savedProperty = propertyService.createProperty(property, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Property created successfully",
                        new PropertyResponse(savedProperty)
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAllProperties() {
        PropertySearchCriteria criteria = new PropertySearchCriteria();
        criteria.setStatus(PropertyStatus.AVAILABLE);

        List<PropertyResponse> properties =
                propertyService.searchPropertiesWithFilters(criteria);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "All available properties retrieved successfully",
                        properties
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAvailablePropertiesByCity(
            @RequestParam String city) {

        List<PropertyResponse> properties = propertyService
                .getAvailablePropertiesByCity(city)
                .stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Properties retrieved successfully",
                        properties
                )
        );
    }
    // Add this endpoint to your PropertyController.java

@GetMapping("/search/v2")
public ResponseEntity<Page<Property>> searchPropertiesV2(
        @ModelAttribute PropertySearchCriteria criteria,
        Pageable pageable
) {
    Page<Property> properties = propertyService.searchProperties(criteria, pageable);
    return ResponseEntity.ok(properties);
}

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> filterProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) java.math.BigDecimal minRent,
            @RequestParam(required = false) java.math.BigDecimal maxRent,
            @RequestParam(required = false)
            outbroker_backend.common.enums.PropertyType propertyType,
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(required = false) Integer bathrooms) {

        PropertySearchCriteria criteria = new PropertySearchCriteria();

        criteria.setCity(city);
        criteria.setMinRent(minRent);
        criteria.setMaxRent(maxRent);
        criteria.setPropertyType(propertyType);
        criteria.setBedrooms(bedrooms);
        criteria.setBathrooms(bathrooms);
        criteria.setStatus(PropertyStatus.AVAILABLE);

        List<PropertyResponse> properties =
                propertyService.searchPropertiesWithFilters(criteria);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Properties filtered successfully",
                        properties
                )
        );
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getNearbyProperties(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radiusKm) {

        List<PropertyResponse> properties =
                propertyService.findNearbyProperties(
                        latitude,
                        longitude,
                        radiusKm
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Nearby properties retrieved successfully",
                        properties
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> getPropertyById(
            @PathVariable UUID id) {

        Property property = propertyService.getPropertyById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Property details retrieved successfully",
                        new PropertyResponse(property)
                )
        );
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByOwner(
            @PathVariable UUID ownerId) {

        List<PropertyResponse> properties = propertyService
                .getPropertiesByOwner(ownerId)
                .stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Owner properties retrieved successfully",
                        properties
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePropertyRequest request,
            @AuthenticationPrincipal User currentUser) {

        Property updatedDetails = new Property();

        // Core fields
        updatedDetails.setTitle(request.getTitle());
        updatedDetails.setDescription(request.getDescription());
        updatedDetails.setMonthlyRent(request.getMonthlyRent());
        updatedDetails.setSecurityDeposit(request.getSecurityDeposit());
        updatedDetails.setMaintenanceFee(request.getMaintenanceFee());

        // Domain specifications
        updatedDetails.setPropertyType(request.getPropertyType());
        updatedDetails.setTransactionType(request.getTransactionType());
        updatedDetails.setFurnishingStatus(request.getFurnishingStatus());
        updatedDetails.setTenantPreference(request.getTenantPreference());

        // Physical parameters
        updatedDetails.setBedrooms(request.getBedrooms());
        updatedDetails.setBathrooms(request.getBathrooms());
        updatedDetails.setPropertyAgeYears(request.getPropertyAgeYears());
        updatedDetails.setFloorNumber(request.getFloorNumber());
        updatedDetails.setTotalFloors(request.getTotalFloors());
        updatedDetails.setFacingDirection(request.getFacingDirection());
        updatedDetails.setParkingSpaces(request.getParkingSpaces());

        // Location & Availability
        updatedDetails.setCity(request.getCity());
        updatedDetails.setAddress(request.getAddress());
        updatedDetails.setLandmark(request.getLandmark());
        updatedDetails.setLatitude(request.getLatitude());
        updatedDetails.setLongitude(request.getLongitude());
        updatedDetails.setAvailabilityDate(request.getAvailabilityDate());

        // Amenities
        updatedDetails.setAmenities(request.getAmenities());

        Property updatedProperty =
                propertyService.updateProperty(
                        id,
                        updatedDetails,
                        currentUser
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Property updated successfully",
                        new PropertyResponse(updatedProperty)
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteProperty(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        propertyService.deleteProperty(id, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Property deleted successfully",
                        null
                )
        );
    }
}