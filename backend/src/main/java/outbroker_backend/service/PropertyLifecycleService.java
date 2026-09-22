package outbroker_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.property.repository.PropertyRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PropertyLifecycleService {

    private final PropertyRepository propertyRepository;

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    @Transactional
    public void expireProperties() {

        LocalDateTime now = LocalDateTime.now();

        var expiredProperties =
                propertyRepository.findExpiredProperties(now);

        if (expiredProperties.isEmpty()) {
            return;
        }

        expiredProperties.forEach(property ->
                property.setStatus(PropertyStatus.EXPIRED)
        );

        propertyRepository.saveAll(expiredProperties);
    }
}