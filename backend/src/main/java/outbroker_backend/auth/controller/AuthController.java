package outbroker_backend.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.auth.dto.AuthResponse;
import outbroker_backend.auth.dto.RefreshTokenRequest;
import outbroker_backend.auth.dto.SendOtpRequest;
import outbroker_backend.auth.dto.VerifyOtpRequest;
import outbroker_backend.auth.service.AuthService;
import outbroker_backend.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "OTP authentication and JWT token management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-otp")
    @Operation(
            summary = "Send OTP",
            description = "Sends an OTP to the provided phone number for authentication."
    )
    @SecurityRequirements
    public ResponseEntity<ApiResponse<String>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {

        authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", null));
    }

    @PostMapping("/verify-otp")
    @Operation(
            summary = "Verify OTP",
            description = "Verifies the OTP and returns access and refresh JWT tokens."
    )
    @SecurityRequirements
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Refresh access token",
            description = "Uses a valid refresh token to issue a new access and refresh token pair."
    )
    @SecurityRequirements
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }
}