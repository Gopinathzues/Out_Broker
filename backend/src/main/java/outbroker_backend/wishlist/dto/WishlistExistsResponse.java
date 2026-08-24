package outbroker_backend.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WishlistExistsResponse {
    private boolean exists;
}