package outbroker_backend.favorite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import outbroker_backend.favorite.entity.FavoriteProperty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoritePropertyRepository extends JpaRepository<FavoriteProperty, UUID> {
    boolean existsByUserIdAndPropertyId(UUID userId, UUID propertyId);
    Optional<FavoriteProperty> findByUserIdAndPropertyId(UUID userId, UUID propertyId);
    List<FavoriteProperty> findByUserId(UUID userId);
}