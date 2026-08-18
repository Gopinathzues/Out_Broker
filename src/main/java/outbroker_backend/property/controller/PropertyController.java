package outbroker_backend.property.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.common.dto.ApiResponse;
import outbroker_backend.property.dto.CreatePropertyRequest;
import outbroker_backend.property.dto.PropertyResponse;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.service.PropertyService;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final UserRepository userRepository;

    public PropertyController(PropertyService propertyService, UserRepository userRepository) {
        this.propertyService = propertyService;
        this.userRepository = userRepository;
    }

    @PostMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @PathVariable UUID ownerId,
            @Valid @RequestBody CreatePropertyRequest request) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + ownerId));

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
                owner
        );

        Property savedProperty = propertyService.createProperty(property);
        return ResponseEntity.ok(ApiResponse.success("Property created successfully", new PropertyResponse(savedProperty)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAvailablePropertiesByCity(@RequestParam String city) {
        List<PropertyResponse> properties = propertyService.getAvailablePropertiesByCity(city)
                .stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Properties retrieved successfully", properties));
    }
}