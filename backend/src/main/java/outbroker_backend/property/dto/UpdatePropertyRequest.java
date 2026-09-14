package outbroker_backend.property.dto;

import outbroker_backend.common.enums.FoodAvailability;
import outbroker_backend.common.enums.FurnishingStatus;
import outbroker_backend.common.enums.HostelGenderPreference;
import outbroker_backend.common.enums.PropertyType;
import outbroker_backend.common.enums.TenantPreference;
import outbroker_backend.common.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class UpdatePropertyRequest {

    private String title;
    private String description;
    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;
    private BigDecimal maintenanceFee;
    private PropertyType propertyType;
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
    private String city;
    private String address;
    private String landmark;
    private double latitude;
    private double longitude;
    private LocalDate availabilityDate;
    private Set<String> amenities;

    // Hostel / PG Fields
    private HostelGenderPreference hostelGenderPreference;
    private FoodAvailability foodAvailability;
    private Boolean isAc;
    private String curfewTime;
    private String allowedStayDuration;

    public UpdatePropertyRequest() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(BigDecimal monthlyRent) { this.monthlyRent = monthlyRent; }

    public BigDecimal getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(BigDecimal securityDeposit) { this.securityDeposit = securityDeposit; }

    public BigDecimal getMaintenanceFee() { return maintenanceFee; }
    public void setMaintenanceFee(BigDecimal maintenanceFee) { this.maintenanceFee = maintenanceFee; }

    public PropertyType getPropertyType() { return propertyType; }
    public void setPropertyType(PropertyType propertyType) { this.propertyType = propertyType; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public FurnishingStatus getFurnishingStatus() { return furnishingStatus; }
    public void setFurnishingStatus(FurnishingStatus furnishingStatus) { this.furnishingStatus = furnishingStatus; }

    public TenantPreference getTenantPreference() { return tenantPreference; }
    public void setTenantPreference(TenantPreference tenantPreference) { this.tenantPreference = tenantPreference; }

    public int getBedrooms() { return bedrooms; }
    public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }

    public int getBathrooms() { return bathrooms; }
    public void setBathrooms(int bathrooms) { this.bathrooms = bathrooms; }

    public Integer getPropertyAgeYears() { return propertyAgeYears; }
    public void setPropertyAgeYears(Integer propertyAgeYears) { this.propertyAgeYears = propertyAgeYears; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public Integer getTotalFloors() { return totalFloors; }
    public void setTotalFloors(Integer totalFloors) { this.totalFloors = totalFloors; }

    public String getFacingDirection() { return facingDirection; }
    public void setFacingDirection(String facingDirection) { this.facingDirection = facingDirection; }

    public Integer getParkingSpaces() { return parkingSpaces; }
    public void setParkingSpaces(Integer parkingSpaces) { this.parkingSpaces = parkingSpaces; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public LocalDate getAvailabilityDate() { return availabilityDate; }
    public void setAvailabilityDate(LocalDate availabilityDate) { this.availabilityDate = availabilityDate; }

    public Set<String> getAmenities() { return amenities; }
    public void setAmenities(Set<String> amenities) { this.amenities = amenities; }

    public HostelGenderPreference getHostelGenderPreference() { return hostelGenderPreference; }
    public void setHostelGenderPreference(HostelGenderPreference hostelGenderPreference) { this.hostelGenderPreference = hostelGenderPreference; }

    public FoodAvailability getFoodAvailability() { return foodAvailability; }
    public void setFoodAvailability(FoodAvailability foodAvailability) { this.foodAvailability = foodAvailability; }

    public Boolean getIsAc() { return isAc; }
    public void setIsAc(Boolean isAc) { this.isAc = isAc; }

    public String getCurfewTime() { return curfewTime; }
    public void setCurfewTime(String curfewTime) { this.curfewTime = curfewTime; }

    public String getAllowedStayDuration() { return allowedStayDuration; }
    public void setAllowedStayDuration(String allowedStayDuration) { this.allowedStayDuration = allowedStayDuration; }
}