package outbroker_backend.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.report.entity.Report;
import outbroker_backend.property.service.PropertyService;
import outbroker_backend.report.service.ReportService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(
        name = "Admin",
        description = "Administrative moderation and report management APIs"
)
public class AdminController {

    private final ReportService reportService;

    public AdminController(PropertyService propertyService, ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    @Operation(
            summary = "Get reports by status",
            description = "Retrieves reports filtered by their current status."
    )
    public ResponseEntity<List<Report>> getAllPendingReports(
            @RequestParam(defaultValue = "PENDING") String status) {

        return ResponseEntity.ok(
                reportService.getReportsByStatus(status)
        );
    }

    @PatchMapping("/reports/{reportId}")
    @Operation(
            summary = "Resolve a report",
            description = "Updates the status of a reported issue."
    )
    public ResponseEntity<Void> resolveReport(
            @PathVariable UUID reportId,
            @RequestParam String status) {

        reportService.updateReportStatus(reportId, status);
        return ResponseEntity.ok().build();
    }
}