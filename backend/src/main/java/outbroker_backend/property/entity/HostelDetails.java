package outbroker_backend.property.entity;

import jakarta.persistence.*;
import outbroker_backend.common.enums.GenderRestriction;
import outbroker_backend.common.enums.RoomType;

import java.util.UUID;

@Entity
@Table(name = "hostel_details", uniqueConstraints = {
        @UniqueConstraint(name = "uk_hostel_details_property", columnNames = { "property_id" })
})
public class HostelDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false, unique = true)
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_restriction", nullable = false)
    private GenderRestriction genderRestriction;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    @Column(name = "food_included")
    private boolean foodIncluded;

    @Column(name = "ac_available")
    private boolean acAvailable;

    @Column(name = "total_beds")
    private Integer totalBeds;

    @Column(name = "available_beds")
    private Integer availableBeds;

    @Column(name = "curfew_time")
    private String curfewTime;

    @Column(name = "notice_period_days")
    private Integer noticePeriodDays;

    public HostelDetails() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public GenderRestriction getGenderRestriction() {
        return genderRestriction;
    }

    public void setGenderRestriction(GenderRestriction genderRestriction) {
        this.genderRestriction = genderRestriction;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public boolean isFoodIncluded() {
        return foodIncluded;
    }

    public void setFoodIncluded(boolean foodIncluded) {
        this.foodIncluded = foodIncluded;
    }

    public boolean isAcAvailable() {
        return acAvailable;
    }

    public void setAcAvailable(boolean acAvailable) {
        this.acAvailable = acAvailable;
    }

    public Integer getTotalBeds() {
        return totalBeds;
    }

    public void setTotalBeds(Integer totalBeds) {
        this.totalBeds = totalBeds;
    }

    public Integer getAvailableBeds() {
        return availableBeds;
    }

    public void setAvailableBeds(Integer availableBeds) {
        this.availableBeds = availableBeds;
    }

    public String getCurfewTime() {
        return curfewTime;
    }

    public void setCurfewTime(String curfewTime) {
        this.curfewTime = curfewTime;
    }

    public Integer getNoticePeriodDays() {
        return noticePeriodDays;
    }

    public void setNoticePeriodDays(Integer noticePeriodDays) {
        this.noticePeriodDays = noticePeriodDays;
    }
}