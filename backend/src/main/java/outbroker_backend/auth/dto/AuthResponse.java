package outbroker_backend.auth.dto;

import outbroker_backend.common.enums.UserRole;
import outbroker_backend.common.enums.VerificationStatus;

import java.util.UUID;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String phoneNumber;
    private UserRole role;
    private VerificationStatus verificationStatus;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, UUID userId, String phoneNumber, UserRole role, VerificationStatus verificationStatus) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.verificationStatus = verificationStatus;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public UUID getUserId() { return userId; }
    public String getPhoneNumber() { return phoneNumber; }
    public UserRole getRole() { return role; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
}