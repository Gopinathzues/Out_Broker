package outbroker_backend.savedsearch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.property.dto.PropertySearchCriteria;
import outbroker_backend.savedsearch.entity.SavedSearch;
import outbroker_backend.savedsearch.service.SavedSearchService;
import outbroker_backend.user.entity.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/saved-searches")
@Tag(
        name = "Saved Searches",
        description = "Save property search criteria and receive matching property notifications"
)
public class SavedSearchController {

    private final SavedSearchService savedSearchService;

    public SavedSearchController(
            SavedSearchService savedSearchService
    ) {
        this.savedSearchService = savedSearchService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TENANT', 'ROLE_TENANT')")
    @Operation(
            summary = "Create saved search",
            description = "Saves property search criteria for the authenticated tenant."
    )
    public ResponseEntity<SavedSearch> createSavedSearch(
            @RequestBody PropertySearchCriteria criteria,
            Authentication authentication
    ) {

        User user = extractUser(authentication);

        return ResponseEntity.ok(
                savedSearchService.createSavedSearch(
                        criteria,
                        user
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TENANT', 'ROLE_TENANT')")
    @Operation(
            summary = "Get my saved searches",
            description = "Returns saved searches belonging to the authenticated tenant."
    )
    public ResponseEntity<List<SavedSearch>> getSavedSearches(
            Authentication authentication
    ) {

        User user = extractUser(authentication);

        return ResponseEntity.ok(
                savedSearchService.getUserSavedSearches(
                        user.getId()
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TENANT', 'ROLE_TENANT')")
    @Operation(
            summary = "Delete saved search",
            description = "Deletes one saved search belonging to the authenticated tenant."
    )
    public ResponseEntity<Void> deleteSavedSearch(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        User user = extractUser(authentication);

        savedSearchService.deleteSavedSearch(
                id,
                user.getId()
        );

        return ResponseEntity.noContent().build();
    }

    private User extractUser(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "User is not authenticated"
            );
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new IllegalStateException(
                "Unable to determine authenticated user"
        );
    }
}