package outbroker_backend.property.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.property.dto.PropertySearchCriteria;
import outbroker_backend.property.entity.Property;

import java.util.ArrayList;
import java.util.List;

public class PropertySpecification {

    public static Specification<Property> buildSpecification(PropertySearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            PropertyStatus statusToFilter = criteria.getStatus() != null ? criteria.getStatus() : PropertyStatus.AVAILABLE;
            predicates.add(cb.equal(root.get("status"), statusToFilter));

            if (criteria.getCity() != null && !criteria.getCity().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("city")), criteria.getCity().trim().toLowerCase()));
            }

            if (criteria.getMinRent() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("monthlyRent"), criteria.getMinRent()));
            }

            if (criteria.getMaxRent() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("monthlyRent"), criteria.getMaxRent()));
            }

            if (criteria.getPropertyType() != null) {
                predicates.add(cb.equal(root.get("propertyType"), criteria.getPropertyType()));
            }

            if (criteria.getBedrooms() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("bedrooms"), criteria.getBedrooms()));
            }

            if (criteria.getBathrooms() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("bathrooms"), criteria.getBathrooms()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}