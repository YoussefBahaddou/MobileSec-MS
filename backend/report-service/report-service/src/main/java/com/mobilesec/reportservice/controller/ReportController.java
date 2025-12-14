package com.mobilesec.reportservice.controller;

import com.mobilesec.reportservice.dto.ComprehensiveReportDto;
import com.mobilesec.reportservice.entity.ReportEntity;
import com.mobilesec.reportservice.rendering.RenderedReport;
import com.mobilesec.reportservice.rendering.ReportFormat;
import com.mobilesec.reportservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow frontend
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody ComprehensiveReportDto dto) {
        try {
            System.out.println("Received Report DTO: " + dto);
            if (dto.getManifest() != null) {
                System.out.println("Manifest Package: " + dto.getManifest().getPackageName());
            } else {
                System.out.println("Manifest is NULL");
            }

            ReportEntity saved = reportService.saveReport(dto);
            // Return the DTO with the generated Scan ID (if new) or confirmation
            return ResponseEntity.ok(reportService.getReport(saved.getScanId()));
        } catch (Exception e) {
            e.printStackTrace(); // Log to console for user to see
            return ResponseEntity.status(500).body("Error saving report: " + e.getMessage());
        }
    }

    @GetMapping("/{scanId}")
    public ResponseEntity<ComprehensiveReportDto> getReport(@PathVariable String scanId) {
        return ResponseEntity.ok(reportService.getReport(scanId));
    }

    @GetMapping("/{scanId}/pdf")
    public ResponseEntity<byte[]> getPdfReport(@PathVariable String scanId) {
        RenderedReport report = reportService.generateReport(scanId, ReportFormat.PDF);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.filename() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(report.content());
    }

    @GetMapping("/{scanId}/sarif")
    public ResponseEntity<byte[]> getSarifReport(@PathVariable String scanId) {
        RenderedReport report = reportService.generateReport(scanId, ReportFormat.SARIF);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.filename() + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(report.content());
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleJsonError(
            org.springframework.http.converter.HttpMessageNotReadableException e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body("JSON Parse Error: " + e.getMessage());
    }
}
