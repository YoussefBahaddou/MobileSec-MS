package com.mobilesec.reportgen.controller;

import com.mobilesec.reportgen.dto.ComprehensiveReportDto;
import com.mobilesec.reportgen.rendering.RenderedReport;
import com.mobilesec.reportgen.rendering.ReportFormat;
import com.mobilesec.reportgen.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody ComprehensiveReportDto reportDto) {
        try {
            reportService.saveReport(reportDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error saving report: " + e.getMessage());
        }
    }

    @GetMapping("/{scanId}")
    public ResponseEntity<ComprehensiveReportDto> getReport(@PathVariable String scanId) {
        return ResponseEntity.ok(reportService.getReport(scanId));
    }

    @GetMapping("/{scanId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String scanId) {
        return download(scanId, ReportFormat.PDF);
    }

    @GetMapping("/{scanId}/sarif")
    public ResponseEntity<byte[]> downloadSarif(@PathVariable String scanId) {
        return download(scanId, ReportFormat.SARIF);
    }

    private ResponseEntity<byte[]> download(String scanId, ReportFormat format) {
        RenderedReport report = reportService.generateReport(scanId, format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.filename() + "\"")
                .contentType(MediaType.parseMediaType(report.contentType()))
                .body(report.content());
    }
}
