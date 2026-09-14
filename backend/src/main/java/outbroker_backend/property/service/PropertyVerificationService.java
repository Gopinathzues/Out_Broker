package outbroker_backend.property.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.common.enums.UserRole;
import outbroker_backend.common.enums.VerificationStatus;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.entity.PropertyVerification;
import outbroker_backend.property.repository.PropertyVerificationRepository;
import outbroker_backend.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PropertyVerificationService {

    private final PropertyVerificationRepository verificationRepository;
    private final PropertyService propertyService;

    public PropertyVerificationService(PropertyVerificationRepository verificationRepository, PropertyService propertyService) {
        this.verificationRepository = verificationRepository;
        this.propertyService = propertyService;
    }

    @Transactional
    public PropertyVerification submitForVerification(UUID propertyId, String documentUrl, User currentUser) {
        Property property = propertyService.getPropertyById(propertyId);

        if (currentUser.getRole() != UserRole.ADMIN && 
            (property.getOwner() == null || !property.getOwner().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("You do not own this property.");
        }

        PropertyVerification verification = verificationRepository.findByPropertyId(propertyId)
                .orElseGet(() -> new PropertyVerification(property, documentUrl));

        verification.setDocumentUrl(documentUrl);
        verification.setVerificationStatus(VerificationStatus.BASIC_VERIFIED);
        verification.setReviewedAt(null);
        verification.setReviewedByAdmin(null);

        return verificationRepository.save(verification);
    }

    @Transactional
    public PropertyVerification reviewPropertyVerification(UUID propertyId, VerificationStatus newStatus, String adminNotes, User adminUser) {
        if (adminUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only admins can review property verification submissions.");
        }

        PropertyVerification verification = verificationRepository.findByPropertyId(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("No verification request found for property ID: " + propertyId));

        verification.setVerificationStatus(newStatus);
        verification.setAdminNotes(adminNotes);
        verification.setReviewedByAdmin(adminUser);
        verification.setReviewedAt(LocalDateTime.now());

        return verificationRepository.save(verification);
    }

    @Transactional(readOnly = true)
    public List<PropertyVerification> getVerificationsByStatus(VerificationStatus status) {
        return verificationRepository.findByVerificationStatus(status);
    }
}