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

        Property property = new Property(
                request.getTitle(),
                request.getDescription(),
                request.getMonthlyRent(),
                request.getSecurityDeposit(),
                request.getPropertyType(),
                request.getBedrooms(),
                request.getBathrooms(),
                request.getLatitude(),
                request.getLongitude(),
                request.getCity(),
                request.getAddress(),
                currentUser
        );

        Property savedProperty = propertyService.createProperty(property, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Property created successfully", new PropertyResponse(savedProperty)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAllProperties() {
        PropertySearchCriteria criteria = new PropertySearchCriteria();
        criteria.setStatus(PropertyStatus.AVAILABLE);

        List<PropertyResponse> properties = propertyService.searchPropertiesWithFilters(criteria);
        return ResponseEntity.ok(ApiResponse.success("All available properties retrieved successfully", properties));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAvailablePropertiesByCity(@RequestParam String city) {
        List<PropertyResponse> properties = propertyService.getAvailablePropertiesByCity(city)
                .stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Properties retrieved successfully", properties));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> filterProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) java.math.BigDecimal minRent,
            @RequestParam(required = false) java.math.BigDecimal maxRent,
            @RequestParam(required = false) outbroker_backend.common.enums.PropertyType propertyType,
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

        List<PropertyResponse> properties = propertyService.searchPropertiesWithFilters(criteria);
        return ResponseEntity.ok(ApiResponse.success("Properties filtered successfully", properties));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getNearbyProperties(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radiusKm) {

        List<PropertyResponse> properties = propertyService.findNearbyProperties(latitude, longitude, radiusKm);
        return ResponseEntity.ok(ApiResponse.success("Nearby properties retrieved successfully", properties));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> getPropertyById(@PathVariable UUID id) {
        Property property = propertyService.getPropertyById(id);
        return ResponseEntity.ok(ApiResponse.success("Property details retrieved successfully", new PropertyResponse(property)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePropertyRequest request,
            @AuthenticationPrincipal User currentUser) {

        Property updatedDetails = new Property();
        updatedDetails.setTitle(request.getTitle());
        updatedDetails.setDescription(request.getDescription());
        updatedDetails.setMonthlyRent(request.getMonthlyRent());
        updatedDetails.setSecurityDeposit(request.getSecurityDeposit());
        updatedDetails.setPropertyType(request.getPropertyType());
        updatedDetails.setBedrooms(request.getBedrooms());
        updatedDetails.setBathrooms(request.getBathrooms());
        updatedDetails.setCity(request.getCity());
        updatedDetails.setAddress(request.getAddress());

        Property updatedProperty = propertyService.updateProperty(id, updatedDetails, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Property updated successfully", new PropertyResponse(updatedProperty)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteProperty(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        propertyService.deleteProperty(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Property deleted successfully", null));
    }
}