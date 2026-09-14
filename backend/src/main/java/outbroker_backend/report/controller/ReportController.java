package outbroker_backend.report.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import outbroker_backend.report.entity.Report;
import outbroker_backend.report.service.ReportService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<Report> submitReport(
            @RequestParam UUID reporterId,
            @RequestParam(required = false) UUID propertyId,
            @RequestParam String reason
    ) {
        Report report = reportService.submitReport(reporterId, propertyId, reason);
        return ResponseEntity.ok(report);
    }

    @GetMapping
    public ResponseEntity<List<Report>> getReportsByStatus(@RequestParam(defaultValue = "PENDING") String status) {
        return ResponseEntity.ok(reportService.getReportsByStatus(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateReportStatus(@PathVariable UUID id, @RequestParam String status) {
        reportService.updateReportStatus(id, status);
        return ResponseEntity.ok().build();
    }
}