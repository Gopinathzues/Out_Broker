package outbroker_backend.wishlist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.common.dto.ApiResponse;
import outbroker_backend.common.exception.UnauthorizedAccessException;
import outbroker_backend.wishlist.dto.WishlistExistsResponse;
import outbroker_backend.wishlist.dto.WishlistResponse;
import outbroker_backend.wishlist.service.WishlistService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{propertyId}")
    @PreAuthorize("hasAnyAuthority('TENANT', 'ROLE_TENANT')")
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(
            Authentication authentication,
            @PathVariable UUID propertyId) {

        UUID tenantId = extractUserId(authentication);
        WishlistResponse response = wishlistService.addToWishlist(tenantId, propertyId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Property added to wishlist successfully", response));
    }

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("hasAnyAuthority('TENANT', 'ROLE_TENANT')")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            Authentication authentication,
            @PathVariable UUID propertyId) {

        UUID tenantId = extractUserId(authentication);
        wishlistService.removeFromWishlist(tenantId, propertyId);
        return ResponseEntity.ok(ApiResponse.success("Property removed from wishlist successfully", null));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TENANT', 'ROLE_TENANT')")
    public ResponseEntity<ApiResponse<Page<WishlistResponse>>> getTenantWishlist(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {

        UUID tenantId = extractUserId(authentication);
        Page<WishlistResponse> wishlist = wishlistService.getTenantWishlist(tenantId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Wishlist retrieved successfully", wishlist));
    }

    @GetMapping("/{propertyId}/exists")
    @PreAuthorize("hasAnyAuthority('TENANT', 'ROLE_TENANT')")
    public ResponseEntity<ApiResponse<WishlistExistsResponse>> checkWishlistExists(
            Authentication authentication,
            @PathVariable UUID propertyId) {

        UUID tenantId = extractUserId(authentication);
        boolean exists = wishlistService.existsInWishlist(tenantId, propertyId);
        return ResponseEntity.ok(ApiResponse.success("Wishlist status checked successfully", new WishlistExistsResponse(exists)));
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedAccessException("User is not authenticated");
        }
        Object principal = authentication.getPrincipal();

        if (principal instanceof UUID uuid) return uuid;
        if (principal instanceof String str) {
            try { return UUID.fromString(str); } catch (IllegalArgumentException ignored) {}
        }
        try {
            var method = principal.getClass().getMethod("getId");
            Object id = method.invoke(principal);
            if (id instanceof UUID uuid) return uuid;
            if (id instanceof String str) return UUID.fromString(str);
        } catch (Exception ignored) {}

        try {
            return UUID.fromString(authentication.getName());
        } catch (Exception e) {
            throw new UnauthorizedAccessException("Could not resolve authenticated user ID");
        }
    }
}