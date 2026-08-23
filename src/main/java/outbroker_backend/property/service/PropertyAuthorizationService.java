package outbroker_backend.property.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import outbroker_backend.common.enums.UserRole;
import outbroker_backend.common.enums.VerificationStatus;
import outbroker_backend.property.entity.Property;
import outbroker_backend.user.entity.User;

@Service
public class PropertyAuthorizationService {

    public void validateCanCreateProperty(User user) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        if (user.getRole() != UserRole.LANDLORD) {
            throw new AccessDeniedException(
                    "Only fully verified landlords or admins can create properties"
            );
        }

        if (user.getVerificationStatus() != VerificationStatus.FULLY_VERIFIED) {
            throw new AccessDeniedException(
                    "Landlord must be fully verified before creating a property"
            );
        }
    }

    public void validateCanModifyProperty(User user, Property property) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        if (user.getRole() != UserRole.LANDLORD) {
            throw new AccessDeniedException(
                    "Only the property owner or admin can modify this property"
            );
        }

        if (user.getVerificationStatus() != VerificationStatus.FULLY_VERIFIED) {
            throw new AccessDeniedException(
                    "Landlord must be fully verified to modify properties"
            );
        }

        if (property.getOwner() == null
                || !property.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You are not authorized to modify this property"
            );
        }
    }
}

