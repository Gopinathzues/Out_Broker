package outbroker_backend.property.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import outbroker_backend.common.dto.ApiResponse;
import outbroker_backend.property.dto.PropertyImageResponse;
import outbroker_backend.property.service.PropertyImageService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@Tag(
        name = "Property Images",
        description = "Property image upload, retrieval, and deletion APIs"
)
public class PropertyImageController {

    private final PropertyImageService imageService;

    public PropertyImageController(PropertyImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(
            value = "/{propertyId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('LANDLORD', 'BROKER', 'ADMIN')")
    @Operation(
            summary = "Upload property image",
            description = "Uploads an image for a property. Supports marking the image as the primary image."
    )
    public ResponseEntity<ApiResponse<PropertyImageResponse>> uploadImage(
            @PathVariable UUID propertyId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPrimary", defaultValue = "false") boolean isPrimary
    ) throws Exception {

        PropertyImageResponse response =
                imageService.uploadImage(propertyId, file, isPrimary);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Image uploaded successfully",
                        response
                ));
    }

    @GetMapping("/{propertyId}/images")
    @Operation(
            summary = "Get property images",
            description = "Retrieves all images associated with a property."
    )
    public ResponseEntity<ApiResponse<List<PropertyImageResponse>>> getImages(
            @PathVariable UUID propertyId) {

        List<PropertyImageResponse> images =
                imageService.getPropertyImages(propertyId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Property images retrieved",
                        images
                )
        );
    }

    @DeleteMapping("/{propertyId}/images/{imageId}")
    @PreAuthorize("hasAnyRole('LANDLORD', 'BROKER', 'ADMIN')")
    @Operation(
            summary = "Delete property image",
            description = "Deletes a specific image associated with a property."
    )
    public ResponseEntity<ApiResponse<String>> deleteImage(
            @PathVariable UUID propertyId,
            @PathVariable UUID imageId) {

        imageService.deleteImage(propertyId, imageId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Image deleted successfully",
                        null
                )
        );
    }
}