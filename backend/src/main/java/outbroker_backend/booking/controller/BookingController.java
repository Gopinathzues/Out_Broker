package outbroker_backend.booking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.booking.dto.BookingResponse;
import outbroker_backend.booking.dto.CreateBookingRequest;
import outbroker_backend.booking.dto.UpdateBookingStatusRequest;
import outbroker_backend.booking.service.BookingService;
import outbroker_backend.common.dto.ApiResponse;
import outbroker_backend.common.exception.UnauthorizedAccessException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(
        name = "Bookings",
        description = "Property visit booking and booking status management APIs"
)
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TENANT', 'ROLE_TENANT')")
    @Operation(
            summary = "Create a booking",
            description = "Creates a property visit booking for the authenticated tenant."
    )
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            Authentication authentication,
            @Valid @RequestBody CreateBookingRequest request) {

        UUID tenantId = extractUserId(authentication);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Visit booked successfully",
                        bookingService.createBooking(tenantId, request)
                )
        );
    }

    @GetMapping("/tenant")
    @PreAuthorize("hasAnyAuthority('TENANT', 'ROLE_TENANT')")
    @Operation(
            summary = "Get tenant bookings",
            description = "Retrieves all bookings created by the authenticated tenant."
    )
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getTenantBookings(
            Authentication authentication) {

        UUID tenantId = extractUserId(authentication);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tenant bookings retrieved",
                        bookingService.getMyBookingsAsTenant(tenantId)
                )
        );
    }

    @GetMapping("/owner")
    @PreAuthorize("""
            hasAnyAuthority(
                'LANDLORD',
                'BROKER',
                'ADMIN',
                'ROLE_LANDLORD',
                'ROLE_BROKER',
                'ROLE_ADMIN'
            )
            """)
    @Operation(
            summary = "Get owner bookings",
            description = "Retrieves all bookings associated with properties owned by the authenticated owner."
    )
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getOwnerBookings(
            Authentication authentication) {

        UUID ownerId = extractUserId(authentication);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Owner bookings retrieved",
                        bookingService.getMyBookingsAsOwner(ownerId)
                )
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Update booking status",
            description = "Updates the status of an existing property visit booking for an authorized participant."
    )
    public ResponseEntity<ApiResponse<BookingResponse>> updateStatus(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBookingStatusRequest request) {

        UUID userId = extractUserId(authentication);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Booking status updated",
                        bookingService.updateBookingStatus(
                                userId,
                                id,
                                request
                        )
                )
        );
    }

    private UUID extractUserId(Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedAccessException(
                    "User is not authenticated"
            );
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UUID uuid) {
            return uuid;
        }

        if (principal instanceof String str) {
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException ignored) {
            }
        }

        try {
            var method = principal.getClass().getMethod("getId");
            Object id = method.invoke(principal);

            if (id instanceof UUID uuid) {
                return uuid;
            }

            if (id instanceof String str) {
                return UUID.fromString(str);
            }

        } catch (Exception ignored) {
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (Exception e) {
            throw new UnauthorizedAccessException(
                    "Could not resolve authenticated user ID"
            );
        }
    }
}