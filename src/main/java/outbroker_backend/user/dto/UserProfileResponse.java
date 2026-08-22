package outbroker_backend.user.dto;

import outbroker_backend.common.enums.UserRole;
import outbroker_backend.common.enums.VerificationStatus;
import outbroker_backend.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserProfileResponse {

    private UUID id;
    private String phoneNumber;
    private String fullName;
    private String email;
    private UserRole role;
    private VerificationStatus verificationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserProfileResponse() {}

    public UserProfileResponse(User user) {
        this.id = user.getId();
        this.phoneNumber = user.getPhoneNumber();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.verificationStatus = user.getVerificationStatus();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    public UUID getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}