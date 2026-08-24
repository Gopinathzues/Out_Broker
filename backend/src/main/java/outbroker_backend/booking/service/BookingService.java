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

    public BookingService(BookingRepository bookingRepository, PropertyRepository propertyRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse createBooking(UUID tenantId, CreateBookingRequest request) {
        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + tenantId));

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + request.getPropertyId()));

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
    public BookingResponse updateBookingStatus(UUID userId, UUID bookingId, UpdateBookingStatusRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        boolean isOwner = booking.getProperty().getOwner().getId().equals(userId);
        boolean isTenant = booking.getTenant().getId().equals(userId);

        if (!isOwner && !isTenant) {
            throw new UnauthorizedAccessException("You are not authorized to update this booking");
        }

        booking.setStatus(request.getStatus().toUpperCase());
        if (request.getNotes() != null) {
            booking.setNotes(request.getNotes());
        }

        return new BookingResponse(bookingRepository.save(booking));
    }
}