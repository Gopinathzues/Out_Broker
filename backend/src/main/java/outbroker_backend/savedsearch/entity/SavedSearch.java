package outbroker_backend.savedsearch.entity;

import jakarta.persistence.*;
import outbroker_backend.common.entity.BaseEntity;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.common.enums.PropertyType;
import outbroker_backend.user.entity.User;

import java.math.BigDecimal;

@Entity
@Table(
        name = "saved_searches",
        indexes = {
                @Index(name = "idx_saved_search_user_id", columnList = "user_id"),
                @Index(name = "idx_saved_search_city", columnList = "city"),
                @Index(name = "idx_saved_search_property_type", columnList = "property_type")
        }
)
public class SavedSearch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String city;

    @Column(precision = 12, scale = 2)
    private BigDecimal minRent;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxRent;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type")
    private PropertyType propertyType;

    private Integer bedrooms;

    private Integer bathrooms;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PropertyStatus status;

    public SavedSearch() {
    }

    public SavedSearch(
            User user,
            String city,
            BigDecimal minRent,
            BigDecimal maxRent,
            PropertyType propertyType,
            Integer bedrooms,
            Integer bathrooms,
            PropertyStatus status
    ) {
        this.user = user;
        this.city = city;
        this.minRent = minRent;
        this.maxRent = maxRent;
        this.propertyType = propertyType;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getMinRent() {
        return minRent;
    }

    public void setMinRent(BigDecimal minRent) {
        this.minRent = minRent;
    }

    public BigDecimal getMaxRent() {
        return maxRent;
    }

    public void setMaxRent(BigDecimal maxRent) {
        this.maxRent = maxRent;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public PropertyStatus getStatus() {
        return status;
    }

    public void setStatus(PropertyStatus status) {
        this.status = status;
    }
}