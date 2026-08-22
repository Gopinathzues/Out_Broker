package outbroker_backend.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyRatingSummary {
    private UUID propertyId;
    private Double averageRating;
    private Long totalReviews;
}