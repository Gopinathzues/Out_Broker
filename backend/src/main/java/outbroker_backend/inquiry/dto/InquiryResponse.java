package outbroker_backend.inquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import outbroker_backend.inquiry.entity.InquiryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryResponse {

    private UUID id;
    private UUID propertyId;
    private UUID tenantId;
    private UUID ownerId;
    private String message;
    private String contactPhone;
    private InquiryStatus status;
    private LocalDateTime createdAt;
}