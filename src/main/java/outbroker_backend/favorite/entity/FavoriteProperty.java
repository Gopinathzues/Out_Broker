package outbroker_backend.favorite.entity;

import jakarta.persistence.*;
import outbroker_backend.common.entity.BaseEntity;
import outbroker_backend.property.entity.Property;
import outbroker_backend.user.entity.User;

@Entity
@Table(
    name = "favorite_properties",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "property_id"})
)
public class FavoriteProperty extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    public FavoriteProperty() {}

    public FavoriteProperty(User user, Property property) {
        this.user = user;
        this.property = property;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }
}