package outbroker_backend.property.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.property.service.ListingLifecycleService;
import outbroker_backend.user.entity.User;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/owner/properties")
@Tag(
        name = "Owner Property Management",
        description = "Owner-specific property listing lifecycle APIs"
)
public class OwnerPropertyController {

    private final ListingLifecycleService listingLifecycleService;

    public OwnerPropertyController(
            ListingLifecycleService listingLifecycleService) {
        this.listingLifecycleService = listingLifecycleService;
    }

    @PostMapping("/{propertyId}/refresh")
    @PreAuthorize("hasAnyRole('LANDLORD', 'ADMIN')")
    @Operation(
            summary = "Refresh property listing",
            description = "Refreshes the authenticated owner's property listing."
    )
    public ResponseEntity<Void> refreshListing(
            @PathVariable UUID propertyId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        listingLifecycleService.refreshListing(
                propertyId,
                currentUser
        );

        return ResponseEntity.ok().build();
    }
}