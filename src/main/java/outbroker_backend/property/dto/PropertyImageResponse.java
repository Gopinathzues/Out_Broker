package outbroker_backend.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class PropertyImageResponse {
    private UUID id;
    private UUID propertyId;
    private String imageUrl;

    @JsonProperty("isPrimary")
    private boolean primary;

    public PropertyImageResponse() {}

    public PropertyImageResponse(UUID id, UUID propertyId, String imageUrl, boolean primary) {
        this.id = id;
        this.propertyId = propertyId;
        this.imageUrl = imageUrl;
        this.primary = primary;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPropertyId() { return propertyId; }
    public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @JsonProperty("isPrimary")
    public boolean isPrimary() { return primary; }

    public void setPrimary(boolean primary) { this.primary = primary; }
}