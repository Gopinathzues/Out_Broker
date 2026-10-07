package outbroker_backend.savedsearch.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.notification.service.NotificationService;
import outbroker_backend.property.dto.PropertySearchCriteria;
import outbroker_backend.property.entity.Property;
import outbroker_backend.savedsearch.entity.SavedSearch;
import outbroker_backend.savedsearch.repository.SavedSearchRepository;
import outbroker_backend.user.entity.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SavedSearchService {

    private final SavedSearchRepository savedSearchRepository;
    private final NotificationService notificationService;

    public SavedSearchService(
            SavedSearchRepository savedSearchRepository,
            NotificationService notificationService
    ) {
        this.savedSearchRepository = savedSearchRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public SavedSearch createSavedSearch(
            PropertySearchCriteria criteria,
            User user
    ) {

        validateCriteria(criteria);

        SavedSearch savedSearch = new SavedSearch(
                user,
                normalizeCity(criteria.getCity()),
                criteria.getMinRent(),
                criteria.getMaxRent(),
                criteria.getPropertyType(),
                criteria.getBedrooms(),
                criteria.getBathrooms(),
                criteria.getStatus()
        );

        return savedSearchRepository.save(savedSearch);
    }

    @Transactional(readOnly = true)
    public List<SavedSearch> getUserSavedSearches(UUID userId) {

        return savedSearchRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void deleteSavedSearch(
            UUID savedSearchId,
            UUID userId
    ) {

        SavedSearch savedSearch =
                savedSearchRepository.findById(savedSearchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Saved search not found"
                                )
                        );

        if (!savedSearch.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You do not have permission to delete this saved search."
            );
        }

        savedSearchRepository.delete(savedSearch);
    }

    /**
     * Called after a new property is created.
     *
     * Only matching saved searches receive a notification.
     *
     * Multiple matching searches belonging to the same user
     * produce only one notification for that property.
     */
    @Transactional
    public void notifyMatchingSavedSearches(Property property) {

        if (property == null) {
            return;
        }

        if (property.getStatus() != PropertyStatus.AVAILABLE) {
            return;
        }

        List<SavedSearch> savedSearches =
                savedSearchRepository.findAll();

        Set<UUID> notifiedUserIds = new HashSet<>();

        for (SavedSearch savedSearch : savedSearches) {

            if (!matches(savedSearch, property)) {
                continue;
            }

            User user = savedSearch.getUser();

            if (user == null || user.getId() == null) {
                continue;
            }

            /*
             * If the same user has multiple matching saved searches,
             * send only one notification for this property.
             */
            if (!notifiedUserIds.add(user.getId())) {
                continue;
            }

            notificationService.sendNotification(
                    user,
                    "New property matching your saved search",
                    buildNotificationMessage(property),
                    "SAVED_SEARCH_MATCH"
            );
        }
    }

    @Transactional
    public void deleteUserSavedSearches(UUID userId) {
        savedSearchRepository.deleteByUserId(userId);
    }

    private boolean matches(
            SavedSearch savedSearch,
            Property property
    ) {

        /*
         * CITY
         */
        if (savedSearch.getCity() != null
                && !savedSearch.getCity().isBlank()) {

            if (property.getCity() == null
                    || !property.getCity()
                    .equalsIgnoreCase(savedSearch.getCity().trim())) {

                return false;
            }
        }

        /*
         * MINIMUM RENT
         */
        if (savedSearch.getMinRent() != null) {

            if (property.getMonthlyRent() == null
                    || property.getMonthlyRent()
                    .compareTo(savedSearch.getMinRent()) < 0) {

                return false;
            }
        }

        /*
         * MAXIMUM RENT
         */
        if (savedSearch.getMaxRent() != null) {

            if (property.getMonthlyRent() == null
                    || property.getMonthlyRent()
                    .compareTo(savedSearch.getMaxRent()) > 0) {

                return false;
            }
        }

        /*
         * PROPERTY TYPE
         */
        if (savedSearch.getPropertyType() != null
                && property.getPropertyType()
                != savedSearch.getPropertyType()) {

            return false;
        }

        /*
         * BEDROOMS
         *
         * Property bedrooms is a primitive int,
         * so there is no null check here.
         *
         * Example:
         * Saved search = 2 bedrooms
         * Property = 3 bedrooms
         * Result = MATCH
         */
        if (savedSearch.getBedrooms() != null
                && property.getBedrooms()
                < savedSearch.getBedrooms()) {

            return false;
        }

        /*
         * BATHROOMS
         *
         * Property bathrooms is a primitive int,
         * so there is no null check here.
         *
         * Example:
         * Saved search = 2 bathrooms
         * Property = 3 bathrooms
         * Result = MATCH
         */
        if (savedSearch.getBathrooms() != null
                && property.getBathrooms()
                < savedSearch.getBathrooms()) {

            return false;
        }

        /*
         * PROPERTY STATUS
         */
        if (savedSearch.getStatus() != null
                && property.getStatus()
                != savedSearch.getStatus()) {

            return false;
        }

        return true;
    }

    private String buildNotificationMessage(Property property) {

        String title = property.getTitle() != null
                ? property.getTitle()
                : "New property";

        String city = property.getCity() != null
                ? property.getCity()
                : "your preferred location";

        return title
                + " is now available in "
                + city
                + ".";
    }

    private void validateCriteria(
            PropertySearchCriteria criteria
    ) {

        if (criteria == null) {
            throw new IllegalArgumentException(
                    "Search criteria are required"
            );
        }

        /*
         * RENT VALIDATION
         */
        if (criteria.getMinRent() != null
                && criteria.getMinRent().signum() < 0) {

            throw new IllegalArgumentException(
                    "Minimum rent cannot be negative"
            );
        }

        if (criteria.getMaxRent() != null
                && criteria.getMaxRent().signum() < 0) {

            throw new IllegalArgumentException(
                    "Maximum rent cannot be negative"
            );
        }

        if (criteria.getMinRent() != null
                && criteria.getMaxRent() != null
                && criteria.getMinRent()
                .compareTo(criteria.getMaxRent()) > 0) {

            throw new IllegalArgumentException(
                    "Minimum rent cannot be greater than maximum rent"
            );
        }

        /*
         * BEDROOM VALIDATION
         */
        if (criteria.getBedrooms() != null
                && criteria.getBedrooms() < 0) {

            throw new IllegalArgumentException(
                    "Bedrooms cannot be negative"
            );
        }

        /*
         * BATHROOM VALIDATION
         */
        if (criteria.getBathrooms() != null
                && criteria.getBathrooms() < 0) {

            throw new IllegalArgumentException(
                    "Bathrooms cannot be negative"
            );
        }
    }

    private String normalizeCity(String city) {

        if (city == null || city.isBlank()) {
            return null;
        }

        return city.trim();
    }
}