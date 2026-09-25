package outbroker_backend.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import outbroker_backend.booking.dto.BookingResponse;
import outbroker_backend.booking.dto.CreateBookingRequest;
import outbroker_backend.booking.dto.UpdateBookingStatusRequest;
import outbroker_backend.booking.entity.Booking;
import outbroker_backend.booking.repository.BookingRepository;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.common.enums.UserRole;
import outbroker_backend.common.exception.UnauthorizedAccessException;
import outbroker_backend.notification.service.NotificationService;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    private BookingService bookingService;

    private UUID tenantId;
    private UUID ownerId;
    private UUID propertyId;
    private UUID bookingId;

    private User tenant;
    private User owner;
    private Property property;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                bookingRepository,
                propertyRepository,
                userRepository,
                notificationService
        );

        tenantId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        bookingId = UUID.randomUUID();

        tenant = mock(User.class);
        owner = mock(User.class);
        property = mock(Property.class);
    }

    // =========================================================
    // CREATE BOOKING
    // =========================================================

    @Test
    void createBooking_shouldCreatePendingBooking() {

        LocalDateTime visitDateTime = LocalDateTime.now().plusDays(2);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPropertyId(propertyId);
        request.setVisitDateTime(visitDateTime);
        request.setNotes("Please call before visiting");

        when(tenant.getRole()).thenReturn(UserRole.TENANT);

        when(userRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(property.getStatus())
                .thenReturn(PropertyStatus.AVAILABLE);

        when(property.getExpiresAt())
                .thenReturn(LocalDateTime.now().plusDays(1));

        when(property.getTitle())
                .thenReturn("Test Property");

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.createBooking(
                tenantId,
                request
        );

        assertNotNull(response);

        verify(bookingRepository).save(argThat(booking ->
                booking.getTenant() == tenant
                        && booking.getProperty() == property
                        && visitDateTime.equals(booking.getVisitDateTime())
                        && "Please call before visiting".equals(booking.getNotes())
                        && "PENDING".equals(booking.getStatus())
        ));

        verify(notificationService).sendNotification(
                eq(owner),
                eq("New booking request"),
                contains("Test Property"),
                eq("BOOKING_CREATED")
        );
    }

    @Test
    void createBooking_shouldRejectNonTenant() {

        User landlord = mock(User.class);

        when(userRepository.findById(tenantId))
                .thenReturn(Optional.of(landlord));

        when(landlord.getRole())
                .thenReturn(UserRole.LANDLORD);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPropertyId(propertyId);
        request.setVisitDateTime(
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> bookingService.createBooking(
                        tenantId,
                        request
                )
        );

        verify(propertyRepository, never())
                .findById(any());

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void createBooking_shouldRejectPastVisitDate() {

        when(userRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(tenant.getRole())
                .thenReturn(UserRole.TENANT);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPropertyId(propertyId);
        request.setVisitDateTime(
                LocalDateTime.now().minusMinutes(1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(
                        tenantId,
                        request
                )
        );

        verify(propertyRepository, never())
                .findById(any());

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void createBooking_shouldRejectOwnProperty() {

        when(userRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(tenant.getRole())
                .thenReturn(UserRole.TENANT);

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(tenantId);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPropertyId(propertyId);
        request.setVisitDateTime(
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(
                UnauthorizedAccessException.class,
                () -> bookingService.createBooking(
                        tenantId,
                        request
                )
        );

        verify(bookingRepository, never())
                .save(any());

        verify(notificationService, never())
                .sendNotification(
                        any(),
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void createBooking_shouldRejectUnavailableProperty() {

        when(userRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(tenant.getRole())
                .thenReturn(UserRole.TENANT);

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(property.getStatus())
                .thenReturn(PropertyStatus.SOLD);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPropertyId(propertyId);
        request.setVisitDateTime(
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(
                        tenantId,
                        request
                )
        );

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void createBooking_shouldRejectExpiredProperty() {

        when(userRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(tenant.getRole())
                .thenReturn(UserRole.TENANT);

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(property.getStatus())
                .thenReturn(PropertyStatus.AVAILABLE);

        when(property.getExpiresAt())
                .thenReturn(LocalDateTime.now().minusMinutes(1));

        CreateBookingRequest request = new CreateBookingRequest();
        request.setPropertyId(propertyId);
        request.setVisitDateTime(
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(
                        tenantId,
                        request
                )
        );

        verify(bookingRepository, never())
                .save(any());
    }

    // =========================================================
    // QUERY
    // =========================================================

    @Test
    void getMyBookingsAsTenant_shouldReturnBookings() {

        Booking booking = mock(Booking.class);

        when(booking.getTenant())
                .thenReturn(tenant);

        when(booking.getProperty())
                .thenReturn(property);

        when(booking.getStatus())
                .thenReturn("PENDING");

        when(booking.getVisitDateTime())
                .thenReturn(LocalDateTime.now().plusDays(1));

        when(property.getId())
                .thenReturn(propertyId);

        when(property.getTitle())
                .thenReturn("Test Property");

        when(bookingRepository
                .findByTenantIdOrderByVisitDateTimeDesc(tenantId))
                .thenReturn(List.of(booking));

        List<BookingResponse> result =
                bookingService.getMyBookingsAsTenant(tenantId);

        assertEquals(1, result.size());

        verify(bookingRepository)
                .findByTenantIdOrderByVisitDateTimeDesc(tenantId);
    }

    @Test
    void getMyBookingsAsOwner_shouldReturnBookings() {

        Booking booking = mock(Booking.class);

        when(booking.getTenant())
                .thenReturn(tenant);

        when(booking.getProperty())
                .thenReturn(property);

        when(booking.getStatus())
                .thenReturn("PENDING");

        when(booking.getVisitDateTime())
                .thenReturn(LocalDateTime.now().plusDays(1));

        when(property.getId())
                .thenReturn(propertyId);

        when(property.getTitle())
                .thenReturn("Test Property");

        when(bookingRepository.findByOwnerId(ownerId))
                .thenReturn(List.of(booking));

        List<BookingResponse> result =
                bookingService.getMyBookingsAsOwner(ownerId);

        assertEquals(1, result.size());

        verify(bookingRepository)
                .findByOwnerId(ownerId);
    }

    // =========================================================
    // UPDATE STATUS — UNAUTHORIZED
    // =========================================================

    @Test
    void updateBookingStatus_shouldRejectUnauthorizedUser() {

        UUID attackerId = UUID.randomUUID();

        Booking booking = mock(Booking.class);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(booking.getStatus())
                .thenReturn("PENDING");

        when(booking.getProperty())
                .thenReturn(property);

        when(booking.getTenant())
                .thenReturn(tenant);

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(tenant.getId())
                .thenReturn(tenantId);

        UpdateBookingStatusRequest request =
                new UpdateBookingStatusRequest();

        request.setStatus("CONFIRMED");

        assertThrows(
                UnauthorizedAccessException.class,
                () -> bookingService.updateBookingStatus(
                        attackerId,
                        bookingId,
                        request
                )
        );

        verify(propertyRepository, never())
                .findByIdForUpdate(any());

        verify(bookingRepository, never())
                .save(any());
    }

    // =========================================================
    // PENDING
    // =========================================================

    @Test
    void updateBookingStatus_ownerCanConfirmPendingBooking() {

        Booking booking = mock(Booking.class);
        Property lockedProperty = mock(Property.class);

        LocalDateTime visitDateTime =
                LocalDateTime.now().plusDays(2);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(booking.getStatus())
                .thenReturn("PENDING");

        when(booking.getProperty())
                .thenReturn(property);

        when(booking.getTenant())
                .thenReturn(tenant);

        when(booking.getVisitDateTime())
                .thenReturn(visitDateTime);

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(tenant.getId())
                .thenReturn(tenantId);

        when(property.getId())
                .thenReturn(propertyId);

        when(propertyRepository.findByIdForUpdate(propertyId))
                .thenReturn(Optional.of(lockedProperty));

        when(lockedProperty.getId())
                .thenReturn(propertyId);

        when(bookingRepository.existsConfirmedBooking(
                propertyId,
                visitDateTime
        )).thenReturn(false);

        when(property.getTitle())
                .thenReturn("Test Property");

        when(bookingRepository.save(booking))
                .thenReturn(booking);

        UpdateBookingStatusRequest request =
                new UpdateBookingStatusRequest();

        request.setStatus("CONFIRMED");

        BookingResponse response =
                bookingService.updateBookingStatus(
                        ownerId,
                        bookingId,
                        request
                );

        assertNotNull(response);

        verify(booking)
                .setStatus("CONFIRMED");

        verify(bookingRepository)
                .save(booking);

        verify(notificationService).sendNotification(
                eq(tenant),
                eq("Booking confirmed"),
                contains("Test Property"),
                eq("BOOKING_CONFIRMED")
        );
    }

    @Test
    void updateBookingStatus_tenantCanCancelPendingBooking() {

        Booking booking = mock(Booking.class);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(booking.getStatus())
                .thenReturn("PENDING");

        when(booking.getProperty())
                .thenReturn(property);

        when(booking.getTenant())
                .thenReturn(tenant);

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(tenant.getId())
                .thenReturn(tenantId);

        when(property.getTitle())
                .thenReturn("Test Property");

        when(bookingRepository.save(booking))
                .thenReturn(booking);

        UpdateBookingStatusRequest request =
                new UpdateBookingStatusRequest();

        request.setStatus("CANCELLED");

        BookingResponse response =
                bookingService.updateBookingStatus(
                        tenantId,
                        bookingId,
                        request
                );

        assertNotNull(response);

        verify(booking)
                .setStatus("CANCELLED");

        verify(bookingRepository)
                .save(booking);

        verify(notificationService).sendNotification(
                eq(owner),
                eq("Booking cancelled"),
                contains("Test Property"),
                eq("BOOKING_CANCELLED")
        );
    }

    @Test
    void updateBookingStatus_tenantCannotConfirmPendingBooking() {

        Booking booking = mock(Booking.class);
        LocalDateTime visitDateTime =
                LocalDateTime.now().plusDays(1);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(booking.getStatus())
                .thenReturn("PENDING");

        when(booking.getProperty())
                .thenReturn(property);

        when(booking.getTenant())
                .thenReturn(tenant);

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(tenant.getId())
                .thenReturn(tenantId);

        when(property.getId())
                .thenReturn(propertyId);

        when(booking.getVisitDateTime())
                .thenReturn(visitDateTime);
        Property lockedProperty1 = mock(Property.class);

when(propertyRepository.findByIdForUpdate(propertyId))
        .thenReturn(Optional.of(lockedProperty1));

when(lockedProperty1.getId())
        .thenReturn(propertyId);

when(bookingRepository.existsConfirmedBooking(
        propertyId,
        visitDateTime
)).thenReturn(false);

        /*
         * BookingService checks authorization before the
         * confirmation concurrency check.
         *
         * Therefore a tenant attempting CONFIRMED must be
         * recognized as the tenant and the method should
         * reject the transition before the property lock.
         */
        UpdateBookingStatusRequest request =
                new UpdateBookingStatusRequest();

        request.setStatus("CONFIRMED");

        assertThrows(
                UnauthorizedAccessException.class,
                () -> bookingService.updateBookingStatus(
                        tenantId,
                        bookingId,
                        request
                )
        );

        

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void updateBookingStatus_ownerCanRejectPendingBooking() {

        Booking booking = mock(Booking.class);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(booking.getStatus())
                .thenReturn("PENDING");

        when(booking.getProperty())
                .thenReturn(property);

        when(booking.getTenant())
                .thenReturn(tenant);

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(tenant.getId())
                .thenReturn(tenantId);

        when(property.getTitle())
                .thenReturn("Test Property");

        when(bookingRepository.save(booking))
                .thenReturn(booking);

        UpdateBookingStatusRequest request =
                new UpdateBookingStatusRequest();

        request.setStatus("REJECTED");

        BookingResponse response =
                bookingService.updateBookingStatus(
                        ownerId,
                        bookingId,
                        request
                );

        assertNotNull(response);

        verify(booking)
                .setStatus("REJECTED");

        verify(bookingRepository)
                .save(booking);

        verify(notificationService).sendNotification(
                eq(tenant),
                eq("Booking rejected"),
                contains("Test Property"),
                eq("BOOKING_REJECTED")
        );
    }

    // =========================================================
    // CONFIRMED
    // =========================================================

    @Test
    void updateBookingStatus_ownerCanCompleteConfirmedBooking() {

        Booking booking = mock(Booking.class);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(booking.getStatus())
                .thenReturn("CONFIRMED");

        when(booking.getProperty())
                .thenReturn(property);

        when(booking.getTenant())
                .thenReturn(tenant);

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(tenant.getId())
                .thenReturn(tenantId);

        when(property.getTitle())
                .thenReturn("Test Property");

        when(bookingRepository.save(booking))
                .thenReturn(booking);

        UpdateBookingStatusRequest request =
                new UpdateBookingStatusRequest();

        request.setStatus("COMPLETED");

        BookingResponse response =
                bookingService.updateBookingStatus(
                        ownerId,
                        bookingId,
                        request
                );

        assertNotNull(response);

        verify(booking)
                .setStatus("COMPLETED");

        verify(bookingRepository)
                .save(booking);

        verify(notificationService).sendNotification(
                eq(tenant),
                eq("Booking completed"),
                contains("Test Property"),
                eq("BOOKING_COMPLETED")
        );
    }

    @Test
    void updateBookingStatus_tenantCannotCompleteConfirmedBooking() {

        Booking booking = mock(Booking.class);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(booking.getStatus())
                .thenReturn("CONFIRMED");

        when(booking.getProperty())
                .thenReturn(property);

        when(booking.getTenant())
                .thenReturn(tenant);

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(tenant.getId())
                .thenReturn(tenantId);

        UpdateBookingStatusRequest request =
                new UpdateBookingStatusRequest();

        request.setStatus("COMPLETED");

        assertThrows(
                UnauthorizedAccessException.class,
                () -> bookingService.updateBookingStatus(
                        tenantId,
                        bookingId,
                        request
                )
        );

        verify(bookingRepository, never())
                .save(any());
    }

    // =========================================================
    // CONCURRENCY
    // =========================================================

    @Test
    void updateBookingStatus_shouldRejectAlreadyConfirmedVisit() {

        Booking booking = mock(Booking.class);
        Property lockedProperty = mock(Property.class);

        LocalDateTime visitDateTime =
                LocalDateTime.now().plusDays(2);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(booking.getStatus())
                .thenReturn("PENDING");

        when(booking.getProperty())
                .thenReturn(property);

        when(booking.getTenant())
                .thenReturn(tenant);

        when(booking.getVisitDateTime())
                .thenReturn(visitDateTime);

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(tenant.getId())
                .thenReturn(tenantId);

        when(property.getId())
                .thenReturn(propertyId);

        when(propertyRepository.findByIdForUpdate(propertyId))
                .thenReturn(Optional.of(lockedProperty));

        when(lockedProperty.getId())
                .thenReturn(propertyId);

        when(bookingRepository.existsConfirmedBooking(
                propertyId,
                visitDateTime
        )).thenReturn(true);

        UpdateBookingStatusRequest request =
                new UpdateBookingStatusRequest();

        request.setStatus("CONFIRMED");

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.updateBookingStatus(
                        ownerId,
                        bookingId,
                        request
                )
        );

        verify(bookingRepository, never())
                .save(any());
    }

    // =========================================================
    // INVALID TRANSITIONS
    // =========================================================

    @Test
    void updateBookingStatus_shouldRejectInvalidStatus() {

        Booking booking = mock(Booking.class);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(booking.getStatus())
                .thenReturn("PENDING");

        when(booking.getProperty())
                .thenReturn(property);

        when(booking.getTenant())
                .thenReturn(tenant);

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(tenant.getId())
                .thenReturn(tenantId);

        UpdateBookingStatusRequest request =
                new UpdateBookingStatusRequest();

        request.setStatus("INVALID_STATUS");

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.updateBookingStatus(
                        ownerId,
                        bookingId,
                        request
                )
        );

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void updateBookingStatus_shouldRejectChangingCompletedBooking() {

        Booking booking = mock(Booking.class);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(booking.getStatus())
                .thenReturn("COMPLETED");

        when(booking.getProperty())
                .thenReturn(property);

        when(booking.getTenant())
                .thenReturn(tenant);

        when(property.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(tenant.getId())
                .thenReturn(tenantId);

        UpdateBookingStatusRequest request =
                new UpdateBookingStatusRequest();

        request.setStatus("CANCELLED");

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.updateBookingStatus(
                        ownerId,
                        bookingId,
                        request
                )
        );

        verify(bookingRepository, never())
                .save(any());
    }
}