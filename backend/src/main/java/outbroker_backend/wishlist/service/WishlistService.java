package outbroker_backend.wishlist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.property.dto.PropertyResponse;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.wishlist.dto.WishlistResponse;
import outbroker_backend.wishlist.entity.Wishlist;
import outbroker_backend.wishlist.repository.WishlistRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final PropertyRepository propertyRepository;

    @Transactional
    public WishlistResponse addToWishlist(UUID tenantId, UUID propertyId) {
        if (wishlistRepository.existsByTenantIdAndPropertyId(tenantId, propertyId)) {
            Wishlist existing = wishlistRepository.findByTenantIdAndPropertyId(tenantId, propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found"));
            return mapToResponse(existing);
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + propertyId));

        Wishlist wishlist = Wishlist.builder()
                .tenantId(tenantId)
                .property(property)
                .build();

        Wishlist saved = wishlistRepository.save(wishlist);
        return mapToResponse(saved);
    }

    @Transactional
    public void removeFromWishlist(UUID tenantId, UUID propertyId) {
        if (!wishlistRepository.existsByTenantIdAndPropertyId(tenantId, propertyId)) {
            throw new ResourceNotFoundException("Property not found in wishlist");
        }
        wishlistRepository.deleteByTenantIdAndPropertyId(tenantId, propertyId);
    }

    @Transactional(readOnly = true)
    public Page<WishlistResponse> getTenantWishlist(UUID tenantId, Pageable pageable) {
        return wishlistRepository.findByTenantId(tenantId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public boolean existsInWishlist(UUID tenantId, UUID propertyId) {
        return wishlistRepository.existsByTenantIdAndPropertyId(tenantId, propertyId);
    }

    private WishlistResponse mapToResponse(Wishlist wishlist) {
        Property property = wishlist.getProperty();
        PropertyResponse propertyDto = new PropertyResponse(property);

        return WishlistResponse.builder()
                .id(wishlist.getId())
                .tenantId(wishlist.getTenantId())
                .property(propertyDto)
                .createdAt(wishlist.getCreatedAt())
                .build();
    }
}