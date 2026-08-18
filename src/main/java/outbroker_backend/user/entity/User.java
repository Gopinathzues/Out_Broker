package outbroker_backend.user.entity;

import jakarta.persistence.*;
import outbroker_backend.common.entity.BaseEntity;
import outbroker_backend.common.enums.UserRole;
import outbroker_backend.common.enums.VerificationStatus;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    private String fullName;
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false) // Maps to 'user_role' column in DB
    private UserRole role = UserRole.TENANT;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    public User() {}

    public User(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.role = UserRole.TENANT;
        this.verificationStatus = VerificationStatus.UNVERIFIED;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
}