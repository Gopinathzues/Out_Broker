package outbroker_backend.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.booking.dto.BookingResponse;
import outbroker_backend.booking.dto.CreateBookingRequest;
import outbroker_backend.booking.dto.UpdateBookingStatusRequest;
import outbroker_backend.booking.entity.Booking;
import outbroker_backend.booking.repository.BookingRepository;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.common.enums.UserRole;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.common.exception.UnauthorizedAccessException;
import outbroker_backend.notification.service.NotificationService;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public BookingService(
            BookingRepository bookingRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository,
            NotificationService notificationService) {

        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public BookingResponse createBooking(
            UUID tenantId,
            CreateBookingRequest request) {

        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + tenantId
                        )
                );

        if (tenant.getRole() != UserRole.TENANT) {
            throw new UnauthorizedAccessException(
                    "Only tenants can create bookings"
            );
        }

        if (request == null
                || request.getPropertyId() == null) {

            throw new IllegalArgumentException(
                    "Property ID is required"
            );
        }

        if (request.getVisitDateTime() == null) {
            throw new IllegalArgumentException(
                    "Visit date and time are required"
            );
        }

        if (request.getVisitDateTime()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Visit date and time cannot be in the past"
            );
        }

        Property property = propertyRepository
                .findById(request.getPropertyId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Property not found with id: "
                                        + request.getPropertyId()
                        )
                );

        if (property.getOwner() == null
                || property.getOwner().getId() == null) {

            throw new IllegalStateException(
                    "Property owner information is missing"
            );
        }

        if (property.getOwner().getId().equals(tenantId)) {
            throw new UnauthorizedAccessException(
                    "You cannot book a visit for your own property"
            );
        }

        if (property.getStatus() != PropertyStatus.AVAILABLE) {
            throw new IllegalArgumentException(
                    "This property is not currently available for booking"
            );
        }

        if (property.getExpiresAt() != null
                && property.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "This property listing has expired"
            );
        }

        Booking booking = new Booking();

        booking.setTenant(tenant);
        booking.setProperty(property);
        booking.setVisitDateTime(request.getVisitDateTime());
        booking.setNotes(request.getNotes());
        booking.setStatus("PENDING");

        Booking savedBooking = bookingRepository.save(booking);

        notificationService.sendNotification(
                property.getOwner(),
                "New booking request",
                buildBookingMessage(
                        property,
                        "You have received a new visit request for "
                ),
                "BOOKING_CREATED"
        );

        return new BookingResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookingsAsTenant(UUID tenantId) {

        return bookingRepository
                .findByTenantIdOrderByVisitDateTimeDesc(tenantId)
                .stream()
                .map(BookingResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookingsAsOwner(UUID ownerId) {

        return bookingRepository
                .findByOwnerId(ownerId)
                .stream()
                .map(BookingResponse::new)
                .toList();
    }

    @Transactional
    public BookingResponse updateBookingStatus(
            UUID userId,
            UUID bookingId,
            UpdateBookingStatusRequest request) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking not found with id: " + bookingId
                        )
                );

        if (request == null
                || request.getStatus() == null
                || request.getStatus().isBlank()) {

            throw new IllegalArgumentException(
                    "Booking status is required"
            );
        }

        if (booking.getStatus() == null
                || booking.getStatus().isBlank()) {

            throw new IllegalStateException(
                    "Booking has an invalid current status"
            );
        }

        boolean isOwner =
                booking.getProperty()
                        .getOwner()
                        .getId()
                        .equals(userId);

        boolean isTenant =
                booking.getTenant()
                        .getId()
                        .equals(userId);

        if (!isOwner && !isTenant) {
            throw new UnauthorizedAccessException(
                    "You are not authorized to update this booking"
            );
        }

        String currentStatus =
                booking.getStatus()
                        .trim()
                        .toUpperCase();

        String newStatus =
                request.getStatus()
                        .trim()
                        .toUpperCase();

        if ("CONFIRMED".equals(newStatus)) {

    Property lockedProperty =
            propertyRepository.findByIdForUpdate(
                    booking.getProperty().getId()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Property not found with id: "
                                    + booking.getProperty().getId()
                    )
            );

    boolean alreadyConfirmed =
            bookingRepository.existsConfirmedBooking(
                    lockedProperty.getId(),
                    booking.getVisitDateTime()
            );

    if (alreadyConfirmed
            && !"CONFIRMED".equals(currentStatus)) {

        throw new IllegalArgumentException(
                "Another booking is already confirmed for this property and visit time"
        );
    }
}

        validateStatusTransition(
                currentStatus,
                newStatus,
                isOwner,
                isTenant
        );

        booking.setStatus(newStatus);

        if (request.getNotes() != null) {
            booking.setNotes(request.getNotes());
        }

        Booking updatedBooking =
                bookingRepository.save(booking);

        sendStatusNotification(
                booking,
                currentStatus,
                newStatus
        );

        return new BookingResponse(updatedBooking);
    }

    private void sendStatusNotification(
            Booking booking,
            String oldStatus,
            String newStatus) {

        if (oldStatus.equals(newStatus)) {
            return;
        }

        User tenant = booking.getTenant();
        User owner = booking.getProperty().getOwner();

        String propertyTitle =
                booking.getProperty().getTitle() != null
                        ? booking.getProperty().getTitle()
                        : "your property";

        switch (newStatus) {

            case "CONFIRMED" -> notificationService.sendNotification(
                    tenant,
                    "Booking confirmed",
                    "Your visit booking for "
                            + propertyTitle
                            + " has been confirmed.",
                    "BOOKING_CONFIRMED"
            );

            case "REJECTED" -> notificationService.sendNotification(
                    tenant,
                    "Booking rejected",
                    "Your visit booking for "
                            + propertyTitle
                            + " was rejected.",
                    "BOOKING_REJECTED"
            );

            case "CANCELLED" -> {

                if ("PENDING".equals(oldStatus)
                        && booking.getTenant().getId()
                        .equals(tenant.getId())) {

                    notificationService.sendNotification(
                            owner,
                            "Booking cancelled",
                            "A tenant cancelled the visit request for "
                                    + propertyTitle
                                    + ".",
                            "BOOKING_CANCELLED"
                    );

                } else {

                    notificationService.sendNotification(
                            tenant,
                            "Booking cancelled",
                            "Your visit booking for "
                                    + propertyTitle
                                    + " has been cancelled.",
                            "BOOKING_CANCELLED"
                    );
                }
            }

            case "COMPLETED" -> notificationService.sendNotification(
                    tenant,
                    "Booking completed",
                    "Your visit booking for "
                            + propertyTitle
                            + " has been marked as completed.",
                    "BOOKING_COMPLETED"
            );

            default -> {
                // No notification required for other transitions.
            }
        }
    }

    private String buildBookingMessage(
            Property property,
            String prefix) {

        String title = property.getTitle() != null
                ? property.getTitle()
                : "your property";

        return prefix + title + ".";
    }

    private void validateStatusTransition(
            String currentStatus,
            String newStatus,
            boolean isOwner,
            boolean isTenant) {

        if (!Set.of(
                "PENDING",
                "CONFIRMED",
                "REJECTED",
                "COMPLETED",
                "CANCELLED"
        ).contains(newStatus)) {

            throw new IllegalArgumentException(
                    "Invalid booking status: " + newStatus
            );
        }

        if ("PENDING".equals(currentStatus)) {

            if (isTenant
                    && !"CANCELLED".equals(newStatus)) {

                throw new UnauthorizedAccessException(
                        "Tenants can only cancel pending bookings"
                );
            }

            if (isOwner
                    && !Set.of(
                            "CONFIRMED",
                            "REJECTED",
                            "CANCELLED"
                    ).contains(newStatus)) {

                throw new IllegalArgumentException(
                        "Invalid transition from PENDING to "
                                + newStatus
                );
            }

            return;
        }

        if ("CONFIRMED".equals(currentStatus)) {

            if (!isOwner) {
                throw new UnauthorizedAccessException(
                        "Only the property owner can update a confirmed booking"
                );
            }

            if (!Set.of(
                    "COMPLETED",
                    "CANCELLED"
            ).contains(newStatus)) {

                throw new IllegalArgumentException(
                        "Invalid transition from CONFIRMED to "
                                + newStatus
                );
            }

            return;
        }

        throw new IllegalArgumentException(
                "Booking status "
                        + currentStatus
                        + " cannot be changed"
        );
    }
}