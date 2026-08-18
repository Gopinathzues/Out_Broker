package outbroker_backend.favorite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.common.dto.ApiResponse;
import outbroker_backend.favorite.service.FavoritePropertyService;
import outbroker_backend.property.dto.PropertyResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
public class FavoritePropertyController {

    private final FavoritePropertyService favoriteService;

    public FavoritePropertyController(FavoritePropertyService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{propertyId}")
    public ResponseEntity<ApiResponse<String>> addFavorite(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID propertyId) {

        favoriteService.addFavorite(userId, propertyId);
        return ResponseEntity.ok(ApiResponse.success("Property added to favorites", null));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<ApiResponse<String>> removeFavorite(
            @RequestAttribute("userId") UUID userId,
            @PathVariable UUID propertyId) {

        favoriteService.removeFavorite(userId, propertyId);
        return ResponseEntity.ok(ApiResponse.success("Property removed from favorites", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getUserFavorites(
            @RequestAttribute("userId") UUID userId) {

        List<PropertyResponse> favorites = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(ApiResponse.success("Favorite properties retrieved successfully", favorites));
    }
}