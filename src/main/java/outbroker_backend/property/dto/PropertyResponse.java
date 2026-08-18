package outbroker_backend.property.dto;

import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.common.enums.PropertyType;
import outbroker_backend.property.entity.Property;

import java.math.BigDecimal;
import java.util.UUID;

public class PropertyResponse {

    private UUID id;
    private String title;
    private String description;
    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;
    private PropertyType propertyType;
    private PropertyStatus status;
    private int bedrooms;
    private int bathrooms;
    private double latitude;
    private double longitude;
    private String city;
    private String address;
    private UUID ownerId;

    public PropertyResponse(Property property) {
        this.id = property.getId();
        this.title = property.getTitle();
        this.description = property.getDescription();
        this.monthlyRent = property.getMonthlyRent();
        this.securityDeposit = property.getSecurityDeposit();
        this.propertyType = property.getPropertyType();
        this.status = property.getStatus();
        this.bedrooms = property.getBedrooms();
        this.bathrooms = property.getBathrooms();
        this.latitude = property.getLatitude();
        this.longitude = property.getLongitude();
        this.city = property.getCity();
        this.address = property.getAddress();
        this.ownerId = property.getOwner() != null ? property.getOwner().getId() : null;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public BigDecimal getSecurityDeposit() { return securityDeposit; }
    public PropertyType getPropertyType() { return propertyType; }
    public PropertyStatus getStatus() { return status; }
    public int getBedrooms() { return bedrooms; }
    public int getBathrooms() { return bathrooms; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public UUID getOwnerId() { return ownerId; }
}