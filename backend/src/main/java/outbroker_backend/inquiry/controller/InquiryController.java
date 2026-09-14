package outbroker_backend.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.common.exception.UnauthorizedAccessException;
import outbroker_backend.inquiry.dto.CreateInquiryRequest;
import outbroker_backend.inquiry.dto.InquiryResponse;
import outbroker_backend.inquiry.dto.UpdateInquiryStatusRequest;
import outbroker_backend.inquiry.service.InquiryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
@Tag(
        name = "Inquiries",
        description = "Property inquiry and lead management APIs"
)
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TENANT', 'LANDLORD', 'BROKER', 'ADMIN', 'ROLE_TENANT', 'ROLE_LANDLORD')")
    @Operation(
            summary = "Create an inquiry",
            description = "Creates a new inquiry for a property."
    )
    public ResponseEntity<InquiryResponse> createInquiry(
            Authentication authentication,
            @Valid @RequestBody CreateInquiryRequest request) {

        UUID tenantId = extractUserId(authentication);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inquiryService.createInquiry(tenantId, request));
    }

    @GetMapping("/tenant")
    @PreAuthorize("hasAnyAuthority('TENANT', 'LANDLORD', 'BROKER', 'ADMIN', 'ROLE_TENANT', 'ROLE_LANDLORD')")
    @Operation(
            summary = "Get tenant inquiries",
            description = "Retrieves all inquiries created by the authenticated tenant."
    )
    public ResponseEntity<List<InquiryResponse>> getTenantInquiries(
            Authentication authentication) {

        UUID tenantId = extractUserId(authentication);

        return ResponseEntity.ok(
                inquiryService.getTenantInquiries(tenantId)
        );
    }

    @GetMapping("/owner")
    @PreAuthorize("hasAnyAuthority('LANDLORD', 'BROKER', 'ADMIN', 'ROLE_LANDLORD', 'ROLE_BROKER')")
    @Operation(
            summary = "Get owner inquiries",
            description = "Retrieves inquiries received by the authenticated property owner."
    )
    public ResponseEntity<List<InquiryResponse>> getOwnerInquiries(
            Authentication authentication) {

        UUID ownerId = extractUserId(authentication);

        return ResponseEntity.ok(
                inquiryService.getOwnerInquiries(ownerId)
        );
    }

    @PatchMapping("/{inquiryId}/status")
    @PreAuthorize("hasAnyAuthority('LANDLORD', 'BROKER', 'ADMIN', 'ROLE_LANDLORD', 'ROLE_BROKER')")
    @Operation(
            summary = "Update inquiry status",
            description = "Updates the status of an inquiry handled by an owner, broker, or admin."
    )
    public ResponseEntity<InquiryResponse> updateInquiryStatus(
            Authentication authentication,
            @PathVariable UUID inquiryId,
            @Valid @RequestBody UpdateInquiryStatusRequest request) {

        UUID ownerId = extractUserId(authentication);

        return ResponseEntity.ok(
                inquiryService.updateInquiryStatus(
                        ownerId,
                        inquiryId,
                        request
                )
        );
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedAccessException("User is not authenticated");
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

            if (id instanceof UUID uuid) return uuid;
            if (id instanceof String str) return UUID.fromString(str);

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