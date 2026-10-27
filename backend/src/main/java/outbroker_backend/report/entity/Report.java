package outbroker_backend.report.entity;

import jakarta.persistence.*;
import outbroker_backend.common.entity.BaseEntity;
import outbroker_backend.property.entity.Property;
import outbroker_backend.user.entity.User;

@Entity
@Table(name = "reports")
public class Report extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, RESOLVED, DISMISSED

    public Report() {}

    public Report(User reporter, Property property, String reason) {
        this.reporter = reporter;
        this.property = property;
        this.reason = reason;
    }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}