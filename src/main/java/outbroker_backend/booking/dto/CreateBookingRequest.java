package outbroker_backend.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class CreateBookingRequest {

    @NotNull(message = "Property ID is required")
    private UUID propertyId;

    @NotNull(message = "Visit date and time is required")
    @Future(message = "Visit date must be in the future")
    private LocalDateTime visitDateTime;

    private String notes;

    public UUID getPropertyId() { return propertyId; }
    public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }

    public LocalDateTime getVisitDateTime() { return visitDateTime; }
    public void setVisitDateTime(LocalDateTime visitDateTime) { this.visitDateTime = visitDateTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}