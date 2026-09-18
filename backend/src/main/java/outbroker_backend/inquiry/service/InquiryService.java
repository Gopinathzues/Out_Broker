package outbroker_backend.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.common.exception.UnauthorizedAccessException;
import outbroker_backend.inquiry.dto.CreateInquiryRequest;
import outbroker_backend.inquiry.dto.InquiryResponse;
import outbroker_backend.inquiry.dto.UpdateInquiryStatusRequest;
import outbroker_backend.inquiry.entity.Inquiry;
import outbroker_backend.inquiry.entity.InquiryStatus;
import outbroker_backend.inquiry.repository.InquiryRepository;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final PropertyRepository propertyRepository;

    @Transactional
    public InquiryResponse createInquiry(UUID tenantId, CreateInquiryRequest request) {
        Property property = propertyRepository.findPublicPropertyById(
                request.getPropertyId(),
                outbroker_backend.common.enums.PropertyStatus.AVAILABLE,
                java.time.LocalDateTime.now()).orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Property not found or no longer available"));

        Inquiry inquiry = Inquiry.builder()
                .propertyId(request.getPropertyId())
                .tenantId(tenantId)
                .ownerId(property.getOwner().getId())
                .message(request.getMessage())
                .contactPhone(request.getContactPhone())
                .status(InquiryStatus.PENDING)
                .build();

        Inquiry saved = inquiryRepository.save(inquiry);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> getTenantInquiries(UUID tenantId) {
        return inquiryRepository.findByTenantId(tenantId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> getOwnerInquiries(UUID ownerId) {
        return inquiryRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InquiryResponse updateInquiryStatus(UUID ownerId, UUID inquiryId, UpdateInquiryStatusRequest request) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found with ID: " + inquiryId));

        if (!inquiry.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedAccessException("Only the property owner can update inquiry status.");
        }

        inquiry.setStatus(request.getStatus());
        Inquiry updated = inquiryRepository.save(inquiry);
        return mapToResponse(updated);
    }

    private InquiryResponse mapToResponse(Inquiry inquiry) {
        return InquiryResponse.builder()
                .id(inquiry.getId())
                .propertyId(inquiry.getPropertyId())
                .tenantId(inquiry.getTenantId())
                .ownerId(inquiry.getOwnerId())
                .message(inquiry.getMessage())
                .contactPhone(inquiry.getContactPhone())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}