package outbroker_backend.property.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ListingLifecycleService {

    private final PropertyRepository propertyRepository;
    private final PropertyAuthorizationService authorizationService;

    public ListingLifecycleService(
            PropertyRepository propertyRepository,
            PropertyAuthorizationService authorizationService) {

        this.propertyRepository = propertyRepository;
        this.authorizationService = authorizationService;
    }

    /**
     * Refreshes a property listing for its authorized owner or an admin.
     *
     * A refresh makes the listing available again and extends
     * its expiration period by 30 days.
     */
    @Transactional
    public void refreshListing(
            UUID propertyId,
            User currentUser) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Property not found with ID: " + propertyId
                        ));

        authorizationService.validateCanModifyProperty(
                currentUser,
                property
        );

        LocalDateTime now = LocalDateTime.now();

        property.setLastRefreshedAt(now);
        property.setExpiresAt(now.plusDays(30));
        property.setStatus(PropertyStatus.AVAILABLE);

        propertyRepository.save(property);
    }

    /**
     * Automatically marks expired listings as EXPIRED.
     *
     * Public discovery APIs must also check expiresAt directly,
     * because this scheduled task only runs once per day.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processExpiredListings() {

        LocalDateTime now = LocalDateTime.now();

        List<Property> expiredProperties =
                propertyRepository.findExpiredProperties(now);

        if (expiredProperties.isEmpty()) {
            return;
        }

        for (Property property : expiredProperties) {
            property.setStatus(PropertyStatus.EXPIRED);
        }

        propertyRepository.saveAll(expiredProperties);
    }
}