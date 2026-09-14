package outbroker_backend.property.entity;

import jakarta.persistence.*;
import outbroker_backend.common.entity.BaseEntity;
import outbroker_backend.common.enums.VerificationStatus;
import outbroker_backend.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "property_verifications")
public class PropertyVerification extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_admin_id")
    private User reviewedByAdmin;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // Constructors
    public PropertyVerification() {}

    public PropertyVerification(Property property, String documentUrl) {
    this.property = property;
    this.documentUrl = documentUrl;
    this.verificationStatus = VerificationStatus.UNVERIFIED;
}

    // Getters and Setters
    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public User getReviewedByAdmin() { return reviewedByAdmin; }
    public void setReviewedByAdmin(User reviewedByAdmin) { this.reviewedByAdmin = reviewedByAdmin; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}