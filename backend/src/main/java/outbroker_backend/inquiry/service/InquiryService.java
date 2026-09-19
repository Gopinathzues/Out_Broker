package outbroker_backend.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.common.enums.UserRole;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.common.exception.UnauthorizedAccessException;
import outbroker_backend.inquiry.dto.CreateInquiryRequest;
import outbroker_backend.inquiry.dto.InquiryResponse;
import outbroker_backend.inquiry.dto.UpdateInquiryStatusRequest;
import outbroker_backend.inquiry.entity.Inquiry;
import outbroker_backend.inquiry.entity.InquiryStatus;
import outbroker_backend.inquiry.repository.InquiryRepository;
import outbroker_backend.notification.service.NotificationService;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public InquiryResponse createInquiry(
            UUID tenantId,
            CreateInquiryRequest request) {

        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + tenantId
                        )
                );

        if (tenant.getRole() != UserRole.TENANT) {
            throw new UnauthorizedAccessException(
                    "Only tenants can create inquiries"
            );
        }

        if (request == null
                || request.getPropertyId() == null) {

            throw new IllegalArgumentException(
                    "Property ID is required"
            );
        }

        Property property = propertyRepository.findPublicPropertyById(
                request.getPropertyId(),
                PropertyStatus.AVAILABLE,
                LocalDateTime.now()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Property not found or no longer available"
                )
        );

        if (property.getOwner() == null
                || property.getOwner().getId() == null) {

            throw new IllegalStateException(
                    "Property owner information is missing"
            );
        }

        Inquiry inquiry = Inquiry.builder()
                .propertyId(request.getPropertyId())
                .tenantId(tenantId)
                .ownerId(property.getOwner().getId())
                .message(request.getMessage())
                .contactPhone(request.getContactPhone())
                .status(InquiryStatus.PENDING)
                .build();

        Inquiry saved = inquiryRepository.save(inquiry);

        notificationService.sendNotification(
                property.getOwner(),
                "New property inquiry",
                "A tenant has sent an inquiry about "
                        + getPropertyTitle(property)
                        + ".",
                "INQUIRY_CREATED"
        );

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
    public InquiryResponse updateInquiryStatus(
            UUID ownerId,
            UUID inquiryId,
            UpdateInquiryStatusRequest request) {

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inquiry not found with ID: " + inquiryId
                        )
                );

        if (!inquiry.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedAccessException(
                    "Only the property owner can update inquiry status."
            );
        }

        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException(
                    "Inquiry status is required"
            );
        }

        InquiryStatus currentStatus = inquiry.getStatus();
        InquiryStatus newStatus = request.getStatus();

        validateStatusTransition(
                currentStatus,
                newStatus
        );

        inquiry.setStatus(newStatus);

        Inquiry updated = inquiryRepository.save(inquiry);

        sendStatusNotification(
                updated,
                currentStatus,
                newStatus
        );

        return mapToResponse(updated);
    }

    private void validateStatusTransition(
            InquiryStatus currentStatus,
            InquiryStatus newStatus) {

        if (currentStatus == null) {
            throw new IllegalStateException(
                    "Inquiry has an invalid current status"
            );
        }

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Inquiry status is required"
            );
        }

        if (currentStatus == newStatus) {
            return;
        }

        switch (currentStatus) {

            case PENDING -> {
                if (!Set.of(
                        InquiryStatus.CONTACTED,
                        InquiryStatus.ACCEPTED,
                        InquiryStatus.REJECTED
                ).contains(newStatus)) {

                    throw new IllegalArgumentException(
                            "Invalid transition from PENDING to "
                                    + newStatus
                    );
                }
            }

            case CONTACTED -> {
                if (!Set.of(
                        InquiryStatus.ACCEPTED,
                        InquiryStatus.REJECTED,
                        InquiryStatus.CLOSED
                ).contains(newStatus)) {

                    throw new IllegalArgumentException(
                            "Invalid transition from CONTACTED to "
                                    + newStatus
                    );
                }
            }

            case ACCEPTED, REJECTED -> {
                if (newStatus != InquiryStatus.CLOSED) {

                    throw new IllegalArgumentException(
                            "Inquiry can only be closed after "
                                    + currentStatus
                    );
                }
            }

            case CLOSED -> throw new IllegalArgumentException(
                    "Closed inquiries cannot be changed"
            );
        }
    }

    private void sendStatusNotification(
            Inquiry inquiry,
            InquiryStatus oldStatus,
            InquiryStatus newStatus) {

        if (oldStatus == newStatus) {
            return;
        }

        User tenant = userRepository.findById(inquiry.getTenantId())
                .orElse(null);

        if (tenant == null) {
            return;
        }

        String message = switch (newStatus) {
            case CONTACTED ->
                    "The property owner has marked your inquiry as contacted.";

            case ACCEPTED ->
                    "Your property inquiry has been accepted.";

            case REJECTED ->
                    "Your property inquiry has been rejected.";

            case CLOSED ->
                    "Your property inquiry has been closed.";

            default ->
                    "Your property inquiry status has been updated.";
        };

        notificationService.sendNotification(
                tenant,
                "Inquiry status updated",
                message,
                "INQUIRY_STATUS_UPDATED"
        );
    }

    private String getPropertyTitle(Property property) {
        return property.getTitle() != null
                ? property.getTitle()
                : "the property";
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