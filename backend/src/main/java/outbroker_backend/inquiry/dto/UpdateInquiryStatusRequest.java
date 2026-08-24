package outbroker_backend.inquiry.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import outbroker_backend.inquiry.entity.InquiryStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInquiryStatusRequest {

    @NotNull(message = "Inquiry status is required")
    private InquiryStatus status;
}