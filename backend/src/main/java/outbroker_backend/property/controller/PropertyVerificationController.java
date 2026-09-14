package outbroker_backend.property.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.common.enums.VerificationStatus;
import outbroker_backend.property.entity.PropertyVerification;
import outbroker_backend.property.service.PropertyVerificationService;
import outbroker_backend.user.entity.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(
        name = "Property Verification",
        description = "Property verification submission and admin review APIs"
)
public class PropertyVerificationController {

    private final PropertyVerificationService verificationService;

    public PropertyVerificationController(
            PropertyVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/properties/{propertyId}/verify")
    @Operation(
            summary = "Submit property for verification",
            description = "Submits a property and its supporting document for verification."
    )
    public ResponseEntity<PropertyVerification> submitVerification(
            @PathVariable UUID propertyId,
            @RequestParam String documentUrl,
            @AuthenticationPrincipal User currentUser) {

        PropertyVerification verification =
                verificationService.submitForVerification(
                        propertyId,
                        documentUrl,
                        currentUser
                );

        return ResponseEntity.ok(verification);
    }

    @PutMapping("/admin/properties/{propertyId}/verify")
    @Operation(
            summary = "Review property verification",
            description = "Allows an administrator to approve or reject a property's verification request."
    )
    public ResponseEntity<PropertyVerification> reviewVerification(
            @PathVariable UUID propertyId,
            @RequestParam VerificationStatus status,
            @RequestParam(required = false) String adminNotes,
            @AuthenticationPrincipal User adminUser) {

        PropertyVerification verification =
                verificationService.reviewPropertyVerification(
                        propertyId,
                        status,
                        adminNotes,
                        adminUser
                );

        return ResponseEntity.ok(verification);
    }

    @GetMapping("/admin/properties/verifications")
    @Operation(
            summary = "Get verification requests by status",
            description = "Retrieves property verification requests matching the specified verification status."
    )
    public ResponseEntity<List<PropertyVerification>> getVerificationsByStatus(
            @RequestParam VerificationStatus status) {

        List<PropertyVerification> verifications =
                verificationService.getVerificationsByStatus(status);

        return ResponseEntity.ok(verifications);
    }
}