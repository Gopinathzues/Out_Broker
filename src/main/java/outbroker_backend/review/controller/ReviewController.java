package outbroker_backend.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.common.exception.UnauthorizedAccessException;
import outbroker_backend.review.dto.PropertyRatingSummary;
import outbroker_backend.review.dto.ReviewRequest;
import outbroker_backend.review.dto.ReviewResponse;
import outbroker_backend.review.service.ReviewService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/properties/{propertyId}/reviews")
    @PreAuthorize("hasAnyAuthority('TENANT', 'ROLE_TENANT')")
    public ResponseEntity<ReviewResponse> addReview(
            Authentication authentication,
            @PathVariable UUID propertyId,
            @Valid @RequestBody ReviewRequest request) {
        UUID tenantId = extractUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.addReview(tenantId, propertyId, request));
    }

    @GetMapping("/properties/{propertyId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getPropertyReviews(
            @PathVariable UUID propertyId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getPropertyReviews(propertyId, pageable));
    }

    @GetMapping("/properties/{propertyId}/rating")
    public ResponseEntity<PropertyRatingSummary> getPropertyRatingSummary(
            @PathVariable UUID propertyId) {
        return ResponseEntity.ok(reviewService.getPropertyRatingSummary(propertyId));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasAnyAuthority('TENANT', 'ROLE_TENANT', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteReview(
            Authentication authentication,
            @PathVariable UUID reviewId) {
        UUID userId = extractUserId(authentication);
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
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