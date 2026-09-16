package outbroker_backend.property.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/properties")
@Tag(
        name = "Properties",
        description = "Property listing, search, filtering, nearby search, and management APIs"
)
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    @Operation(
            summary = "Create a property",
            description = "Creates a new property listing for an authenticated landlord or admin."
    )
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

        Property savedProperty =
                propertyService.createProperty(property, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Property created successfully",
                        new PropertyResponse(savedProperty)
                ));
    }

    @GetMapping
    @Operation(
            summary = "Get all available properties",
            description = "Retrieves all currently available property listings."
    )
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
    @Operation(
            summary = "Search properties by city",
            description = "Retrieves available properties located in the specified city."
    )
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

    @GetMapping("/search/v2")
    @Operation(
            summary = "Search properties with pagination",
            description = "Searches properties using dynamic criteria with pagination and sorting."
    )
    public ResponseEntity<Page<PropertyResponse>> searchPropertiesV2(
            @ModelAttribute PropertySearchCriteria criteria,
            Pageable pageable) {

        Page<Property> properties =
                propertyService.searchProperties(criteria, pageable);

        Page<PropertyResponse> response =
                properties.map(PropertyResponse::new);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    @Operation(
            summary = "Filter properties",
            description = "Filters available properties using city, rent range, property type, bedrooms, and bathrooms."
    )
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
    @Operation(
            summary = "Find nearby properties",
            description = "Finds available properties within the specified radius of a latitude and longitude."
    )
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
    @Operation(
            summary = "Get property by ID",
            description = "Retrieves detailed information for a specific property."
    )
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
    @Operation(
            summary = "Get properties by owner",
            description = "Retrieves properties belonging to the specified owner."
    )
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
    @Operation(
            summary = "Update a property",
            description = "Updates an existing property listing. Requires landlord or admin authorization."
    )
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePropertyRequest request,
            @AuthenticationPrincipal User currentUser) {

        Property updatedDetails = new Property();

        updatedDetails.setTitle(request.getTitle());
        updatedDetails.setDescription(request.getDescription());
        updatedDetails.setMonthlyRent(request.getMonthlyRent());
        updatedDetails.setSecurityDeposit(request.getSecurityDeposit());
        updatedDetails.setMaintenanceFee(request.getMaintenanceFee());

        updatedDetails.setPropertyType(request.getPropertyType());
        updatedDetails.setTransactionType(request.getTransactionType());
        updatedDetails.setFurnishingStatus(request.getFurnishingStatus());
        updatedDetails.setTenantPreference(request.getTenantPreference());

        updatedDetails.setBedrooms(request.getBedrooms());
        updatedDetails.setBathrooms(request.getBathrooms());
        updatedDetails.setPropertyAgeYears(request.getPropertyAgeYears());
        updatedDetails.setFloorNumber(request.getFloorNumber());
        updatedDetails.setTotalFloors(request.getTotalFloors());
        updatedDetails.setFacingDirection(request.getFacingDirection());
        updatedDetails.setParkingSpaces(request.getParkingSpaces());

        updatedDetails.setCity(request.getCity());
        updatedDetails.setAddress(request.getAddress());
        updatedDetails.setLandmark(request.getLandmark());
        updatedDetails.setLatitude(request.getLatitude());
        updatedDetails.setLongitude(request.getLongitude());
        updatedDetails.setAvailabilityDate(request.getAvailabilityDate());

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
    @Operation(
            summary = "Delete a property",
            description = "Deletes a property listing. Requires landlord or admin authorization."
    )
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