package outbroker_backend.report.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import outbroker_backend.common.exception.ResourceNotFoundException;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.repository.PropertyRepository;
import outbroker_backend.report.entity.Report;
import outbroker_backend.report.repository.ReportRepository;
import outbroker_backend.user.entity.User;
import outbroker_backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private static final String PENDING = "PENDING";
    private static final String RESOLVED = "RESOLVED";
    private static final String DISMISSED = "DISMISSED";

    private final ReportRepository reportRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public ReportService(
            ReportRepository reportRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository) {

        this.reportRepository = reportRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Report submitReport(
            UUID reporterId,
            UUID propertyId,
            String reason) {

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Report reason cannot be empty");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reporter not found"));

        Property property = null;

        if (propertyId != null) {
            property = propertyRepository.findById(propertyId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Property not found"));
        }

        Report report = new Report(
                reporter,
                property,
                reason.trim()
        );

        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<Report> getReportsByStatus(String status) {

        validateStatus(status);

        return reportRepository.findByStatus(status);
    }

    @Transactional
    public void updateReportStatus(
            UUID reportId,
            String newStatus) {

        validateStatus(newStatus);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Report not found"));

        String currentStatus = report.getStatus();

        if (currentStatus.equals(newStatus)) {
            return;
        }

        if (!currentStatus.equals(PENDING)) {
            throw new IllegalStateException(
                    "Only pending reports can be resolved or dismissed"
            );
        }

        report.setStatus(newStatus);
        reportRepository.save(report);
    }

    private void validateStatus(String status) {

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Report status cannot be empty");
        }

        String normalizedStatus = status.trim().toUpperCase();

        if (!normalizedStatus.equals(PENDING)
                && !normalizedStatus.equals(RESOLVED)
                && !normalizedStatus.equals(DISMISSED)) {

            throw new IllegalArgumentException(
                    "Invalid report status. Allowed values: PENDING, RESOLVED, DISMISSED"
            );
        }
    }
}