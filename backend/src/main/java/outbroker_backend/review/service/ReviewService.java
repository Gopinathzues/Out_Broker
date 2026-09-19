package outbroker_backend.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.booking.repository.BookingRepository;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.common.exception.UnauthorizedAccessException;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.review.dto.PropertyRatingSummary;
import outbroker_backend.review.dto.ReviewRequest;
import outbroker_backend.review.dto.ReviewResponse;
import outbroker_backend.review.entity.Review;
import outbroker_backend.review.repository.ReviewRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public ReviewResponse addReview(
            UUID tenantId,
            UUID propertyId,
            ReviewRequest request) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Property not found with ID: " + propertyId
                        )
                );

        boolean hasCompletedBooking =
                bookingRepository.existsCompletedBooking(
                        tenantId,
                        propertyId
                );

        if (!hasCompletedBooking) {
            throw new UnauthorizedAccessException(
                    "You can review a property only after completing a visit booking"
            );
        }

        if (reviewRepository.existsByTenantIdAndPropertyId(
                tenantId,
                propertyId)) {

            throw new IllegalArgumentException(
                    "You have already reviewed this property"
            );
        }

        Review review = Review.builder()
                .tenantId(tenantId)
                .property(property)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getPropertyReviews(
            UUID propertyId,
            Pageable pageable) {

        return reviewRepository
                .findByPropertyId(propertyId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public PropertyRatingSummary getPropertyRatingSummary(
            UUID propertyId) {

        Double avg =
                reviewRepository.getAverageRatingByPropertyId(
                        propertyId
                );

        Long count =
                reviewRepository.countByPropertyId(propertyId);

        double finalAvg =
                (avg != null)
                        ? Math.round(avg * 10.0) / 10.0
                        : 0.0;

        return new PropertyRatingSummary(
                propertyId,
                finalAvg,
                count
        );
    }

    @Transactional
    public void deleteReview(
            UUID reviewId,
            UUID userId,
            boolean isAdmin) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Review not found with ID: " + reviewId
                        )
                );

        if (!isAdmin
                && !review.getTenantId().equals(userId)) {

            throw new UnauthorizedAccessException(
                    "You are not authorized to delete this review"
            );
        }

        reviewRepository.delete(review);
    }

    private ReviewResponse mapToResponse(Review review) {

        return ReviewResponse.builder()
                .id(review.getId())
                .tenantId(review.getTenantId())
                .propertyId(review.getProperty().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}