package outbroker_backend.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import outbroker_backend.property.dto.PropertyResponse;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {
    private UUID id;
    private UUID tenantId;
    private PropertyResponse property;
    private LocalDateTime createdAt;
}