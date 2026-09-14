package outbroker_backend.verification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.common.dto.ApiResponse;
import outbroker_backend.common.exception.UnauthorizedAccessException;
import outbroker_backend.verification.dto.DocumentSubmissionRequest;
import outbroker_backend.verification.dto.ProofSubmissionRequest;
import outbroker_backend.verification.dto.VerificationStatusResponse;
import outbroker_backend.verification.service.VerificationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/verification")
@Tag(
        name = "User Verification",
        description = "User identity and verification submission APIs"
)
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get verification status",
            description = "Retrieves the verification status of the authenticated user."
    )
    public ResponseEntity<ApiResponse<VerificationStatusResponse>> getStatus(
            Authentication authentication) {

        UUID userId = extractUserId(authentication);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification status retrieved",
                        verificationService.getVerificationStatus(userId)
                )
        );
    }

    @PostMapping("/documents")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Submit verification document",
            description = "Submits a document for user identity verification."
    )
    public ResponseEntity<ApiResponse<VerificationStatusResponse>> submitDocument(
            Authentication authentication,
            @Valid @RequestBody DocumentSubmissionRequest request) {

        UUID userId = extractUserId(authentication);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Document submitted successfully",
                        verificationService.submitDocument(userId, request)
                )
        );
    }

    @PostMapping("/proof")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Submit physical proof and selfie",
            description = "Submits physical proof and selfie information for full user verification."
    )
    public ResponseEntity<ApiResponse<VerificationStatusResponse>> submitProof(
            Authentication authentication,
            @Valid @RequestBody ProofSubmissionRequest request) {

        UUID userId = extractUserId(authentication);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Physical proof & selfie submitted successfully",
                        verificationService.submitProof(userId, request)
                )
        );
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedAccessException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UUID uuid) return uuid;

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