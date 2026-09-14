package outbroker_backend.property.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.property.service.ListingLifecycleService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/owner/properties")
public class OwnerPropertyController {

    private final ListingLifecycleService listingLifecycleService;

    public OwnerPropertyController(ListingLifecycleService listingLifecycleService) {
        this.listingLifecycleService = listingLifecycleService;
    }

    @PostMapping("/{propertyId}/refresh")
    public ResponseEntity<Void> refreshListing(
            @PathVariable UUID propertyId,
            @RequestParam UUID ownerId
    ) {
        listingLifecycleService.refreshListing(propertyId, ownerId);
        return ResponseEntity.ok().build();
    }
}