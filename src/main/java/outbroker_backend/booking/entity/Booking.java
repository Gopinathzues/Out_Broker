package outbroker_backend.booking.entity;

import jakarta.persistence.*;
import outbroker_backend.common.entity.BaseEntity;
import outbroker_backend.property.entity.Property;
import outbroker_backend.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    @Column(nullable = false)
    private LocalDateTime visitDateTime;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, CONFIRMED, REJECTED, COMPLETED, CANCELLED

    private String notes;

    public Booking() {}

    @PrePersist
    public void prePersist() {
        if (getId() == null) {
            setId(UUID.randomUUID());
        }
    }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public User getTenant() { return tenant; }
    public void setTenant(User tenant) { this.tenant = tenant; }

    public LocalDateTime getVisitDateTime() { return visitDateTime; }
    public void setVisitDateTime(LocalDateTime visitDateTime) { this.visitDateTime = visitDateTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}