package outbroker_backend.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.property.entity.Property;
import outbroker_backend.property.service.PropertyService;
import outbroker_backend.report.entity.Report;
import outbroker_backend.report.service.ReportService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final PropertyService propertyService;
    private final ReportService reportService;

    public AdminController(PropertyService propertyService, ReportService reportService) {
        this.propertyService = propertyService;
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public ResponseEntity<List<Report>> getAllPendingReports(@RequestParam(defaultValue = "PENDING") String status) {
        return ResponseEntity.ok(reportService.getReportsByStatus(status));
    }

    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<Void> resolveReport(@PathVariable UUID reportId, @RequestParam String status) {
        reportService.updateReportStatus(reportId, status);
        return ResponseEntity.ok().build();
    }
}