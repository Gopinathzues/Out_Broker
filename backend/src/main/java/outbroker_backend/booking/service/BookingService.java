package outbroker_backend.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.booking.dto.BookingResponse;
import outbroker_backend.booking.dto.CreateBookingRequest;
import outbroker_backend.booking.dto.UpdateBookingStatusRequest;
import outbroker_backend.booking.entity.Booking;
import outbroker_backend.booking.repository.BookingRepository;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.common.exception.UnauthorizedAccessException;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, PropertyRepository propertyRepository,
            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse createBooking(UUID tenantId, CreateBookingRequest request) {
        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + tenantId));

        if (tenant.getRole() != outbroker_backend.common.enums.UserRole.TENANT) {
            throw new UnauthorizedAccessException(
                    "Only tenants can create bookings");
        }

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Property not found with id: " + request.getPropertyId()));

        if (request.getVisitDateTime() == null) {
            throw new IllegalArgumentException("Visit date and time are required");
        }

        if (request.getVisitDateTime().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Visit date and time cannot be in the past");
        }

        Booking booking = new Booking();
        booking.setTenant(tenant);
        booking.setProperty(property);
        booking.setVisitDateTime(request.getVisitDateTime());
        booking.setNotes(request.getNotes());
        booking.setStatus("PENDING");

        return new BookingResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookingsAsTenant(UUID tenantId) {
        return bookingRepository.findByTenantIdOrderByVisitDateTimeDesc(tenantId)
                .stream()
                .map(BookingResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookingsAsOwner(UUID ownerId) {
        return bookingRepository.findByOwnerId(ownerId)
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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId));

        boolean isOwner = booking.getProperty().getOwner().getId().equals(userId);

        boolean isTenant = booking.getTenant().getId().equals(userId);

        if (!isOwner && !isTenant) {
            throw new UnauthorizedAccessException(
                    "You are not authorized to update this booking");
        }

        String currentStatus = booking.getStatus().toUpperCase();
        String newStatus = request.getStatus().trim().toUpperCase();
        if ("CONFIRMED".equals(newStatus)) {
            boolean alreadyConfirmed = bookingRepository.existsConfirmedBooking(
                    booking.getProperty().getId(),
                    booking.getVisitDateTime());

            if (alreadyConfirmed && !"CONFIRMED".equals(currentStatus)) {
                throw new IllegalArgumentException(
                        "Another booking is already confirmed for this property and visit time");
            }
        }
        validateStatusTransition(
                currentStatus,
                newStatus,
                isOwner,
                isTenant);

        booking.setStatus(newStatus);

        if (request.getNotes() != null) {
            booking.setNotes(request.getNotes());
        }

        return new BookingResponse(bookingRepository.save(booking));
    }

    private void validateStatusTransition(
            String currentStatus,
            String newStatus,
            boolean isOwner,
            boolean isTenant) {

        if (!java.util.Set.of(
                "PENDING",
                "CONFIRMED",
                "REJECTED",
                "COMPLETED",
                "CANCELLED").contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid booking status: " + newStatus);
        }

        if ("PENDING".equals(currentStatus)) {

            if (isTenant && !"CANCELLED".equals(newStatus)) {
                throw new UnauthorizedAccessException(
                        "Tenants can only cancel pending bookings");
            }

            if (isOwner && !java.util.Set.of(
                    "CONFIRMED",
                    "REJECTED",
                    "CANCELLED").contains(newStatus)) {
                throw new IllegalArgumentException(
                        "Invalid transition from PENDING to " + newStatus);
            }

            return;
        }

        if ("CONFIRMED".equals(currentStatus)) {

            if (!isOwner) {
                throw new UnauthorizedAccessException(
                        "Only the property owner can update a confirmed booking");
            }

            if (!java.util.Set.of(
                    "COMPLETED",
                    "CANCELLED").contains(newStatus)) {
                throw new IllegalArgumentException(
                        "Invalid transition from CONFIRMED to " + newStatus);
            }

            return;
        }

        throw new IllegalArgumentException(
                "Booking status " + currentStatus + " cannot be changed");
    }
}