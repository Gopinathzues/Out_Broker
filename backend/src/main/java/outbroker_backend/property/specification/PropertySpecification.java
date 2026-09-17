package outbroker_backend.property.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.property.dto.PropertySearchCriteria;
import outbroker_backend.property.entity.Property;

import java.util.ArrayList;
import java.util.List;

public class PropertySpecification {

        private PropertySpecification() {
                // Utility class
        }

        public static Specification<Property> buildSpecification(PropertySearchCriteria criteria) {
                return (root, query, criteriaBuilder) -> {

                        List<Predicate> predicates = new ArrayList<>();

                        // Only show available properties by default
                        PropertyStatus statusToFilter = criteria.getStatus() != null
                                        ? criteria.getStatus()
                                        : PropertyStatus.AVAILABLE;

                        predicates.add(
                                        criteriaBuilder.equal(
                                                        root.get("status"),
                                                        statusToFilter));
                        if (statusToFilter == PropertyStatus.AVAILABLE) {
                                predicates.add(
                                                criteriaBuilder.or(
                                                                criteriaBuilder.isNull(root.get("expiresAt")),
                                                                criteriaBuilder.greaterThan(
                                                                                root.get("expiresAt"),
                                                                                java.time.LocalDateTime.now())));
                        }

                        // City
                        if (criteria.getCity() != null
                                        && !criteria.getCity().trim().isEmpty()) {

                                predicates.add(
                                                criteriaBuilder.equal(
                                                                criteriaBuilder.lower(root.get("city")),
                                                                criteria.getCity().trim().toLowerCase()));
                        }

                        // Minimum rent
                        if (criteria.getMinRent() != null) {

                                predicates.add(
                                                criteriaBuilder.greaterThanOrEqualTo(
                                                                root.get("monthlyRent"),
                                                                criteria.getMinRent()));
                        }

                        // Maximum rent
                        if (criteria.getMaxRent() != null) {

                                predicates.add(
                                                criteriaBuilder.lessThanOrEqualTo(
                                                                root.get("monthlyRent"),
                                                                criteria.getMaxRent()));
                        }

                        // Property type
                        if (criteria.getPropertyType() != null) {

                                predicates.add(
                                                criteriaBuilder.equal(
                                                                root.get("propertyType"),
                                                                criteria.getPropertyType()));
                        }

                        // Bedrooms
                        if (criteria.getBedrooms() != null) {

                                predicates.add(
                                                criteriaBuilder.greaterThanOrEqualTo(
                                                                root.get("bedrooms"),
                                                                criteria.getBedrooms()));
                        }

                        // Bathrooms
                        if (criteria.getBathrooms() != null) {

                                predicates.add(
                                                criteriaBuilder.greaterThanOrEqualTo(
                                                                root.get("bathrooms"),
                                                                criteria.getBathrooms()));
                        }

                        return criteriaBuilder.and(
                                        predicates.toArray(new Predicate[0]));
                };
        }
}
