package outbroker_backend.property.dto;

import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.common.enums.PropertyType;

import java.math.BigDecimal;

public class PropertySearchCriteria {

    private String city;
    private BigDecimal minRent;
    private BigDecimal maxRent;
    private PropertyType propertyType;
    private Integer bedrooms;
    private Integer bathrooms;
    private PropertyStatus status;

    // Getters and Setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public BigDecimal getMinRent() { return minRent; }
    public void setMinRent(BigDecimal minRent) { this.minRent = minRent; }

    public BigDecimal getMaxRent() { return maxRent; }
    public void setMaxRent(BigDecimal maxRent) { this.maxRent = maxRent; }

    public PropertyType getPropertyType() { return propertyType; }
    public void setPropertyType(PropertyType propertyType) { this.propertyType = propertyType; }

    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }

    public Integer getBathrooms() { return bathrooms; }
    public void setBathrooms(Integer bathrooms) { this.bathrooms = bathrooms; }

    public PropertyStatus getStatus() { return status; }
    public void setStatus(PropertyStatus status) { this.status = status; }
}