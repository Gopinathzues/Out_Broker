package outbroker_backend.booking.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateBookingStatusRequest {

    @NotBlank(message = "Status is required")
    private String status; // CONFIRMED, REJECTED, COMPLETED, CANCELLED

    private String notes;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}