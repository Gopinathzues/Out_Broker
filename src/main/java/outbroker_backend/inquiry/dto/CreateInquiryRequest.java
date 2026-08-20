package outbroker_backend.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInquiryRequest {

    @NotNull(message = "Property ID is required")
    private UUID propertyId;

    @NotBlank(message = "Message is required")
    private String message;

    @NotBlank(message = "Contact phone is required")
    private String contactPhone;
}