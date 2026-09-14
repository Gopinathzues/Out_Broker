package outbroker_backend.property.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.property.service.ListingLifecycleService;

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
    @Operation(
            summary = "Refresh property listing",
            description = "Refreshes an owner's property listing and updates its listing lifecycle."
    )
    public ResponseEntity<Void> refreshListing(
            @PathVariable UUID propertyId,
            @RequestParam UUID ownerId
    ) {

        listingLifecycleService.refreshListing(
                propertyId,
                ownerId
        );

        return ResponseEntity.ok().build();
    }
}