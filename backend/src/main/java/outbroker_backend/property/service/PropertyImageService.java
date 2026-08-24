package outbroker_backend.property.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import outbroker_backend.common.exception.AccessDeniedException;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.property.dto.PropertyImageResponse;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.entity.PropertyImage;
import outbroker_backend.property.repository.PropertyImageRepository;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.user.entity.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PropertyImageService {

    private final PropertyImageRepository imageRepository;
    private final PropertyRepository propertyRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public PropertyImageService(PropertyImageRepository imageRepository, PropertyRepository propertyRepository) {
        this.imageRepository = imageRepository;
        this.propertyRepository = propertyRepository;
    }

    @Transactional
public PropertyImageResponse uploadImage(UUID propertyId, MultipartFile file, boolean isPrimary) throws IOException {
    Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + propertyId));

    validateOwnership(property);

    // If new image is primary, set all existing images for this property to non-primary
    if (isPrimary) {
        List<PropertyImage> existingImages = imageRepository.findByPropertyId(propertyId);
        for (PropertyImage img : existingImages) {
            if (Boolean.TRUE.equals(img.isPrimary())) {
                img.setPrimary(false);
                imageRepository.save(img);
            }
        }
    }

    // Save physical file
    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
    Path targetPath = Paths.get(uploadDir, "properties", propertyId.toString(), fileName);
    Files.createDirectories(targetPath.getParent());
    Files.copy(file.getInputStream(), targetPath);

    // Save entity
    PropertyImage image = new PropertyImage();
    image.setPropertyId(propertyId);
    image.setImageUrl("/uploads/properties/" + propertyId + "/" + fileName);
    image.setPrimary(isPrimary);

    PropertyImage savedImage = imageRepository.save(image);
    return mapToResponse(savedImage);
}

    @Transactional(readOnly = true)
    public List<PropertyImageResponse> getPropertyImages(UUID propertyId) {
        return imageRepository.findByPropertyId(propertyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteImage(UUID propertyId, UUID imageId) {
        PropertyImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with ID: " + imageId));

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + propertyId));

        validateOwnership(property);

        // 1. Delete physical file from disk
        deletePhysicalFile(image.getImageUrl());

        // 2. Delete database record
        imageRepository.delete(image);
    }

    private void validateOwnership(Property property) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        User currentUser = (User) auth.getPrincipal();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        if (!isAdmin && !property.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to modify images for this property");
        }
    }

    private void deletePhysicalFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        try {
            String relativePath = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;
            Path filePath = Paths.get(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete physical file: " + e.getMessage());
        }
    }

    private PropertyImageResponse mapToResponse(PropertyImage image) {
        return new PropertyImageResponse(
                image.getId(),
                image.getPropertyId(),
                image.getImageUrl(),
                image.isPrimary()
        );
    }
}