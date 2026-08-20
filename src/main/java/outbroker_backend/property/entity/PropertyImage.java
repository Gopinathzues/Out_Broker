package outbroker_backend.property.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "property_images")
public class PropertyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    public PropertyImage() {}

    public PropertyImage(UUID propertyId, String imageUrl, boolean primary) {
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

    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }
}