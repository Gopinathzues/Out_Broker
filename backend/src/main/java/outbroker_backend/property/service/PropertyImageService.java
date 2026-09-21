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

        private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

        private final PropertyImageRepository imageRepository;
        private final PropertyRepository propertyRepository;

        @Value("${file.upload-dir:uploads}")
        private String uploadDir;

        public PropertyImageService(
                        PropertyImageRepository imageRepository,
                        PropertyRepository propertyRepository) {
                this.imageRepository = imageRepository;
                this.propertyRepository = propertyRepository;
        }

        @Transactional
        public PropertyImageResponse uploadImage(
                        UUID propertyId,
                        MultipartFile file,
                        boolean isPrimary) throws IOException {

                Property property = propertyRepository.findById(propertyId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Property not found with ID: " + propertyId));

                validateOwnership(property);
                validateImage(file);

                if (isPrimary) {
                        List<PropertyImage> existingImages = imageRepository.findByPropertyId(propertyId);

                        for (PropertyImage img : existingImages) {
                                if (Boolean.TRUE.equals(img.isPrimary())) {
                                        img.setPrimary(false);
                                        imageRepository.save(img);
                                }
                        }
                }

                String originalFilename = file.getOriginalFilename();
                String extension = getSafeExtension(originalFilename);

                String fileName = UUID.randomUUID() + extension;

                Path uploadRoot = Paths.get(uploadDir)
                                .toAbsolutePath()
                                .normalize();

                Path targetPath = uploadRoot
                                .resolve("properties")
                                .resolve(propertyId.toString())
                                .resolve(fileName)
                                .normalize();

                if (!targetPath.startsWith(uploadRoot)) {
                        throw new IOException("Invalid file path");
                }

                Files.createDirectories(targetPath.getParent());
                
                try (var inputStream = file.getInputStream()) {
                        Files.copy(inputStream, targetPath);
                }

                PropertyImage image = new PropertyImage();
                image.setPropertyId(propertyId);
                image.setImageUrl(
                                "/uploads/properties/"
                                                + propertyId
                                                + "/"
                                                + fileName);
                image.setPrimary(isPrimary);

                PropertyImage savedImage = imageRepository.save(image);

                return mapToResponse(savedImage);
        }

        @Transactional(readOnly = true)
        public List<PropertyImageResponse> getPropertyImages(UUID propertyId) {
                return imageRepository.findByPropertyId(propertyId)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Transactional
        public void deleteImage(UUID propertyId, UUID imageId) {

                PropertyImage image = imageRepository
                                .findByIdAndPropertyId(imageId, propertyId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Image not found for this property"));

                Property property = propertyRepository.findById(propertyId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Property not found with ID: " + propertyId));

                validateOwnership(property);

                deletePhysicalFile(image.getImageUrl());

                imageRepository.delete(image);
        }

        private void validateImage(MultipartFile file) throws IOException {

                if (file == null || file.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Image file cannot be empty");
                }

                if (file.getSize() > MAX_FILE_SIZE) {
                        throw new IllegalArgumentException(
                                        "Image file size cannot exceed 10 MB");
                }

                String originalFilename = file.getOriginalFilename();

                String extension = getSafeExtension(originalFilename);

                byte[] header = new byte[12];

                try (var inputStream = file.getInputStream()) {

                        int bytesRead = inputStream.read(header);

                        if (bytesRead < 12) {
                                throw new IllegalArgumentException(
                                                "Invalid or corrupted image file");
                        }
                }

                boolean isJpeg = (header[0] & 0xFF) == 0xFF
                                && (header[1] & 0xFF) == 0xD8
                                && (header[2] & 0xFF) == 0xFF;

                boolean isPng = (header[0] & 0xFF) == 0x89
                                && (header[1] & 0xFF) == 0x50
                                && (header[2] & 0xFF) == 0x4E
                                && (header[3] & 0xFF) == 0x47
                                && (header[4] & 0xFF) == 0x0D
                                && (header[5] & 0xFF) == 0x0A
                                && (header[6] & 0xFF) == 0x1A
                                && (header[7] & 0xFF) == 0x0A;

                boolean isWebp = header[0] == 'R'
                                && header[1] == 'I'
                                && header[2] == 'F'
                                && header[3] == 'F'
                                && header[8] == 'W'
                                && header[9] == 'E'
                                && header[10] == 'B'
                                && header[11] == 'P';

                boolean validSignature = switch (extension) {
                        case ".jpg", ".jpeg" -> isJpeg;
                        case ".png" -> isPng;
                        case ".webp" -> isWebp;
                        default -> false;
                };

                if (!validSignature) {
                        throw new IllegalArgumentException(
                                        "File content does not match the image type");
                }
        }

        private String getSafeExtension(String originalFilename) {

                if (originalFilename == null
                                || originalFilename.isBlank()) {
                        throw new IllegalArgumentException(
                                        "Image filename is required");
                }

                String filename = Paths.get(originalFilename)
                                .getFileName()
                                .toString();

                int dotIndex = filename.lastIndexOf('.');

                if (dotIndex <= 0 || dotIndex == filename.length() - 1) {
                        throw new IllegalArgumentException(
                                        "Image must have a valid file extension");
                }

                String extension = filename.substring(dotIndex).toLowerCase();

                if (!extension.equals(".jpg")
                                && !extension.equals(".jpeg")
                                && !extension.equals(".png")
                                && !extension.equals(".webp")) {

                        throw new IllegalArgumentException(
                                        "Only JPG, JPEG, PNG and WebP images are allowed");
                }

                return extension;
        }

        private void validateOwnership(Property property) {

                Authentication auth = SecurityContextHolder.getContext()
                                .getAuthentication();

                if (auth == null || !auth.isAuthenticated()) {
                        throw new AccessDeniedException(
                                        "User is not authenticated");
                }

                User currentUser = (User) auth.getPrincipal();

                boolean isAdmin = auth.getAuthorities()
                                .stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                                                || a.getAuthority().equals("ADMIN"));

                if (!isAdmin
                                && !property.getOwner().getId()
                                                .equals(currentUser.getId())) {

                        throw new AccessDeniedException(
                                        "You do not have permission to modify images for this property");
                }
        }

        private void deletePhysicalFile(String imageUrl) {

                if (imageUrl == null || imageUrl.isBlank()) {
                        return;
                }

                try {
                        String relativePath = imageUrl.startsWith("/")
                                        ? imageUrl.substring(1)
                                        : imageUrl;

                        Path uploadRoot = Paths.get(uploadDir)
                                        .toAbsolutePath()
                                        .normalize();

                        Path filePath = uploadRoot
                                        .resolve(
                                                        relativePath.startsWith("uploads/")
                                                                        ? relativePath.substring("uploads/".length())
                                                                        : relativePath)
                                        .normalize();

                        if (!filePath.startsWith(uploadRoot)) {
                                throw new IOException(
                                                "Invalid file deletion path");
                        }

                        Files.deleteIfExists(filePath);

                } catch (IOException e) {
                        System.err.println(
                                        "Failed to delete physical file: "
                                                        + e.getMessage());
                }
        }

        private PropertyImageResponse mapToResponse(
                        PropertyImage image) {

                return new PropertyImageResponse(
                                image.getId(),
                                image.getPropertyId(),
                                image.getImageUrl(),
                                image.isPrimary());
        }
}