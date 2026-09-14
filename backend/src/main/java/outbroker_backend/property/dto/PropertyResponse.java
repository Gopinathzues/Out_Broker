package outbroker_backend.property.dto;

import outbroker_backend.common.enums.*;
import outbroker_backend.property.entity.Property;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public class PropertyResponse {

    private UUID id;
    private String title;
    private String description;
    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;
    private BigDecimal maintenanceFee;
    private PropertyType propertyType;
    private PropertyStatus status;
    private TransactionType transactionType;
    private FurnishingStatus furnishingStatus;
    private TenantPreference tenantPreference;
    private int bedrooms;
    private int bathrooms;
    private Integer propertyAgeYears;
    private Integer floorNumber;
    private Integer totalFloors;
    private String facingDirection;
    private Integer parkingSpaces;
    private double latitude;
    private double longitude;
    private String city;
    private String address;
    private String landmark;
    private LocalDate availabilityDate;
    private Set<String> amenities;
    private UUID ownerId;
    private HostelGenderPreference hostelGenderPreference;
    private FoodAvailability foodAvailability;
    private Boolean isAc;
    private String curfewTime;
    private String allowedStayDuration;

    public PropertyResponse(Property property) {
        this.id = property.getId();
        this.title = property.getTitle();
        this.description = property.getDescription();
        this.monthlyRent = property.getMonthlyRent();
        this.securityDeposit = property.getSecurityDeposit();
        this.maintenanceFee = property.getMaintenanceFee();
        this.propertyType = property.getPropertyType();
        this.status = property.getStatus();
        this.transactionType = property.getTransactionType();
        this.furnishingStatus = property.getFurnishingStatus();
        this.tenantPreference = property.getTenantPreference();
        this.bedrooms = property.getBedrooms();
        this.bathrooms = property.getBathrooms();
        this.propertyAgeYears = property.getPropertyAgeYears();
        this.floorNumber = property.getFloorNumber();
        this.totalFloors = property.getTotalFloors();
        this.facingDirection = property.getFacingDirection();
        this.parkingSpaces = property.getParkingSpaces();
        this.latitude = property.getLatitude();
        this.longitude = property.getLongitude();
        this.city = property.getCity();
        this.address = property.getAddress();
        this.landmark = property.getLandmark();
        this.availabilityDate = property.getAvailabilityDate();
        this.amenities = property.getAmenities();
        this.ownerId = property.getOwner() != null ? property.getOwner().getId() : null;
        this.hostelGenderPreference = property.getHostelGenderPreference();
    this.foodAvailability = property.getFoodAvailability();
    this.isAc = property.getIsAc();
    this.curfewTime = property.getCurfewTime();
    this.allowedStayDuration = property.getAllowedStayDuration();
    
    }

    // Getters
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public BigDecimal getSecurityDeposit() { return securityDeposit; }
    public BigDecimal getMaintenanceFee() { return maintenanceFee; }
    public PropertyType getPropertyType() { return propertyType; }
    public PropertyStatus getStatus() { return status; }
    public TransactionType getTransactionType() { return transactionType; }
    public FurnishingStatus getFurnishingStatus() { return furnishingStatus; }
    public TenantPreference getTenantPreference() { return tenantPreference; }
    public int getBedrooms() { return bedrooms; }
    public int getBathrooms() { return bathrooms; }
    public Integer getPropertyAgeYears() { return propertyAgeYears; }
    public Integer getFloorNumber() { return floorNumber; }
    public Integer getTotalFloors() { return totalFloors; }
    public String getFacingDirection() { return facingDirection; }
    public Integer getParkingSpaces() { return parkingSpaces; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public String getLandmark() { return landmark; }
    public LocalDate getAvailabilityDate() { return availabilityDate; }
    public Set<String> getAmenities() { return amenities; }
    public UUID getOwnerId() { return ownerId; }
    public HostelGenderPreference getHostelGenderPreference() { return hostelGenderPreference; }
    public FoodAvailability getFoodAvailability() { return foodAvailability; }
    public Boolean getIsAc() { return isAc; }
    public String getCurfewTime() { return curfewTime; }
    public String getAllowedStayDuration() { return allowedStayDuration; }
}