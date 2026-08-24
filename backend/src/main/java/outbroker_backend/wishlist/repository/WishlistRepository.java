package outbroker_backend.wishlist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import outbroker_backend.wishlist.entity.Wishlist;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

    boolean existsByTenantIdAndPropertyId(UUID tenantId, UUID propertyId);

    Optional<Wishlist> findByTenantIdAndPropertyId(UUID tenantId, UUID propertyId);

    Page<Wishlist> findByTenantId(UUID tenantId, Pageable pageable);

    void deleteByTenantIdAndPropertyId(UUID tenantId, UUID propertyId);
}