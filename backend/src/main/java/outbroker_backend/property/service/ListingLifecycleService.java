package outbroker_backend.property.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ListingLifecycleService {

    private final PropertyRepository propertyRepository;

    public ListingLifecycleService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Transactional
    public void refreshListing(UUID propertyId, UUID ownerId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized to refresh this listing");
        }

        property.setLastRefreshedAt(LocalDateTime.now());
        property.setExpiresAt(LocalDateTime.now().plusDays(30));
        property.setStatus(PropertyStatus.AVAILABLE);
        propertyRepository.save(property);
    }

    // Runs daily at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processExpiredListings() {
        LocalDateTime now = LocalDateTime.now();
        List<Property> expiredProperties = propertyRepository.findExpiredProperties(now);

        for (Property property : expiredProperties) {
            property.setStatus(PropertyStatus.EXPIRED);
            propertyRepository.save(property);
        }
    }
}