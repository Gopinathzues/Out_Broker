package outbroker_backend.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.booking.entity.Booking;
import outbroker_backend.booking.repository.BookingRepository;
import outbroker_backend.common.enums.PropertyStatus;
import outbroker_backend.common.enums.UserRole;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.user.dto.UpdateProfileRequest;
import outbroker_backend.user.dto.UserProfileResponse;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.entity.UserSession;
import outbroker_backend.user.repository.UserRepository;
import outbroker_backend.user.repository.UserSessionRepository;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final UserSessionRepository userSessionRepository;

    public UserService(
            UserRepository userRepository,
            PropertyRepository propertyRepository,
            BookingRepository bookingRepository,
            UserSessionRepository userSessionRepository
    ) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.bookingRepository = bookingRepository;
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        )
                );

        return new UserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(
            UUID userId,
            UpdateProfileRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        )
                );

        if (request.getFullName() != null
                && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getEmail() != null
                && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail().trim().toLowerCase());
        }

        User updatedUser = userRepository.save(user);

        return new UserProfileResponse(updatedUser);
    }

    @Transactional
    public void deleteAccount(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        )
                );

        validateAccountDeletion(user);

        userSessionRepository.revokeAllActiveUserSessions(user);

        user.setPhoneNumber("deleted_" + UUID.randomUUID());
        user.setFullName("Deleted User");
        user.setEmail(null);

        userRepository.save(user);
    }

    private void validateAccountDeletion(User user) {

        UUID userId = user.getId();

        List<Booking> tenantBookings =
                bookingRepository.findByTenantIdOrderByVisitDateTimeDesc(userId);

        if (tenantBookings.stream().anyMatch(this::isActiveBooking)) {
            throw new IllegalStateException(
                    "Account cannot be deleted while you have pending or confirmed bookings."
            );
        }

        if (user.getRole() == UserRole.LANDLORD) {

            List<Property> properties =
                    propertyRepository.findByOwnerId(userId);

            boolean hasActiveProperty = properties.stream()
                    .anyMatch(property ->
                            property.getStatus() == PropertyStatus.AVAILABLE
                    );

            if (hasActiveProperty) {
                throw new IllegalStateException(
                        "Account cannot be deleted while you have active property listings."
                );
            }

            List<Booking> ownerBookings =
                    bookingRepository.findByOwnerId(userId);

            if (ownerBookings.stream().anyMatch(this::isActiveBooking)) {
                throw new IllegalStateException(
                        "Account cannot be deleted while you have pending or confirmed property bookings."
                );
            }
        }
    }

    private boolean isActiveBooking(Booking booking) {

        String status = booking.getStatus();

        return "PENDING".equalsIgnoreCase(status)
                || "CONFIRMED".equalsIgnoreCase(status);
    }
}