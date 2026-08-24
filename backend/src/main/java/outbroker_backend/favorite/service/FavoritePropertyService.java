package outbroker_backend.favorite.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.favorite.entity.FavoriteProperty;
import outbroker_backend.favorite.repository.FavoritePropertyRepository;
import outbroker_backend.property.dto.PropertyResponse;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FavoritePropertyService {

    private final FavoritePropertyRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    public FavoritePropertyService(FavoritePropertyRepository favoriteRepository,
                                  UserRepository userRepository,
                                  PropertyRepository propertyRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    @Transactional
    public void addFavorite(UUID userId, UUID propertyId) {
        if (favoriteRepository.existsByUserIdAndPropertyId(userId, propertyId)) {
            throw new IllegalArgumentException("Property is already in favorites.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + propertyId));

        favoriteRepository.save(new FavoriteProperty(user, property));
    }

    @Transactional
    public void removeFavorite(UUID userId, UUID propertyId) {
        FavoriteProperty favorite = favoriteRepository.findByUserIdAndPropertyId(userId, propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Favorite entry not found."));
        favoriteRepository.delete(favorite);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getUserFavorites(UUID userId) {
        return favoriteRepository.findByUserId(userId)
                .stream()
                .map(favorite -> new PropertyResponse(favorite.getProperty()))
                .collect(Collectors.toList());
    }
}