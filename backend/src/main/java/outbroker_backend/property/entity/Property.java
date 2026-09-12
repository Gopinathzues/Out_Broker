package outbroker_backend.property.entity;

import jakarta.persistence.*;
import outbroker_backend.common.entity.BaseEntity;
import outbroker_backend.common.enums.*;
import outbroker_backend.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "properties")
public class Property extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "monthly_rent", precision = 12, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "security_deposit", precision = 12, scale = 2)
    private BigDecimal securityDeposit;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false)
    private PropertyType propertyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status = PropertyStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType = TransactionType.RENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "furnishing_status")
    private FurnishingStatus furnishingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_preference")
    private TenantPreference tenantPreference = TenantPreference.ANY;

    @Column(name = "maintenance_fee", precision = 10, scale = 2)
    private BigDecimal maintenanceFee;

    private int bedrooms;
    private int bathrooms;

    @Column(name = "property_age_years")
    private Integer propertyAgeYears;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(name = "total_floors")
    private Integer totalFloors;

    @Column(name = "facing_direction")
    private String facingDirection;

    @Column(name = "parking_spaces")
    private Integer parkingSpaces;

    private double latitude;
    private double longitude;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String address;

    private String landmark;

    @Column(name = "availability_date")
    private LocalDate availabilityDate;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_refreshed_at")
    private LocalDateTime lastRefreshedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToOne(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private HostelDetails hostelDetails;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "property_amenities", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "amenity")
    private Set<String> amenities = new HashSet<>();

    // No-arg Constructor required by JPA
    public Property() {}

    // Multi-arg Constructor required by PropertyController
    public Property(String title, String description, BigDecimal monthlyRent, 
                    BigDecimal securityDeposit, PropertyType propertyType, 
                    int bedrooms, int bathrooms, double latitude, 
                    double longitude, String city, String address, User owner) {
        this.title = title;
        this.description = description;
        this.monthlyRent = monthlyRent;
        this.securityDeposit = securityDeposit;
        this.propertyType = propertyType;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.address = address;
        this.owner = owner;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(BigDecimal monthlyRent) { this.monthlyRent = monthlyRent; }

    public BigDecimal getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(BigDecimal securityDeposit) { this.securityDeposit = securityDeposit; }

    public PropertyType getPropertyType() { return propertyType; }
    public void setPropertyType(PropertyType propertyType) { this.propertyType = propertyType; }

    public PropertyStatus getStatus() { return status; }
    public void setStatus(PropertyStatus status) { this.status = status; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public FurnishingStatus getFurnishingStatus() { return furnishingStatus; }
    public void setFurnishingStatus(FurnishingStatus furnishingStatus) { this.furnishingStatus = furnishingStatus; }

    public TenantPreference getTenantPreference() { return tenantPreference; }
    public void setTenantPreference(TenantPreference tenantPreference) { this.tenantPreference = tenantPreference; }

    public BigDecimal getMaintenanceFee() { return maintenanceFee; }
    public void setMaintenanceFee(BigDecimal maintenanceFee) { this.maintenanceFee = maintenanceFee; }

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

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public LocalDate getAvailabilityDate() { return availabilityDate; }
    public void setAvailabilityDate(LocalDate availabilityDate) { this.availabilityDate = availabilityDate; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getLastRefreshedAt() { return lastRefreshedAt; }
    public void setLastRefreshedAt(LocalDateTime lastRefreshedAt) { this.lastRefreshedAt = lastRefreshedAt; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public HostelDetails getHostelDetails() { return hostelDetails; }
    public void setHostelDetails(HostelDetails hostelDetails) {
        this.hostelDetails = hostelDetails;
        if (hostelDetails != null) {
            hostelDetails.setProperty(this);
        }
    }

    public Set<String> getAmenities() { return amenities; }
    public void setAmenities(Set<String> amenities) { this.amenities = amenities; }

    public BigDecimal getPrice() {
        return monthlyRent != null ? monthlyRent : BigDecimal.ZERO;
    }
}