package outbroker_backend.booking.dto;

import outbroker_backend.booking.entity.Booking;

import java.time.LocalDateTime;
import java.util.UUID;

public class BookingResponse {

    private UUID id;
    private UUID propertyId;
    private String propertyTitle;
    private UUID tenantId;
    private String tenantName;
    private String tenantPhone;
    private LocalDateTime visitDateTime;
    private String status;
    private String notes;

    public BookingResponse(Booking booking) {
        this.id = booking.getId();
        this.propertyId = booking.getProperty().getId();
        this.propertyTitle = booking.getProperty().getTitle();
        this.tenantId = booking.getTenant().getId();
        this.tenantName = booking.getTenant().getFullName();
        this.tenantPhone = booking.getTenant().getPhoneNumber();
        this.visitDateTime = booking.getVisitDateTime();
        this.status = booking.getStatus();
        this.notes = booking.getNotes();
    }

    public UUID getId() { return id; }
    public UUID getPropertyId() { return propertyId; }
    public String getPropertyTitle() { return propertyTitle; }
    public UUID getTenantId() { return tenantId; }
    public String getTenantName() { return tenantName; }
    public String getTenantPhone() { return tenantPhone; }
    public LocalDateTime getVisitDateTime() { return visitDateTime; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
}