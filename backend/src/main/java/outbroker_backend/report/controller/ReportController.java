package outbroker_backend.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.report.entity.Report;
import outbroker_backend.report.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import outbroker_backend.user.entity.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(
        name = "Reports",
        description = "Property and user report submission and moderation APIs"
)
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

   @PostMapping
@PreAuthorize("isAuthenticated()")
@Operation(
        summary = "Submit a report",
        description = "Creates a report for a property or other reported issue."
)
public ResponseEntity<Report> submitReport(
        @RequestParam(required = false) UUID propertyId,
        @RequestParam String reason,
        @AuthenticationPrincipal User currentUser) {

    Report report =
            reportService.submitReport(
                    currentUser.getId(),
                    propertyId,
                    reason
            );

    return ResponseEntity.ok(report);
}

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get reports by status",
            description = "Retrieves reports filtered by their current status."
    )
    public ResponseEntity<List<Report>> getReportsByStatus(
            @RequestParam(defaultValue = "PENDING") String status) {

        return ResponseEntity.ok(
                reportService.getReportsByStatus(status)
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update report status",
            description = "Updates the status of an existing report."
    )
    public ResponseEntity<Void> updateReportStatus(
            @PathVariable UUID id,
            @RequestParam String status) {

        reportService.updateReportStatus(id, status);
        return ResponseEntity.ok().build();
    }
}