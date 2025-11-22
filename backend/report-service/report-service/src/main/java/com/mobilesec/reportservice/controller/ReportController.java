package com.mobilesec.reportservice.controller;

import com.mobilesec.reportservice.rendering.RenderedReport;
import com.mobilesec.reportservice.rendering.ReportFormat;
import com.mobilesec.reportservice.service.ReportGenerationService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@Validated
public class ReportController {

    private final ReportGenerationService reportGenerationService;

    public ReportController(ReportGenerationService reportGenerationService) {
        this.reportGenerationService = reportGenerationService;
    }

    @PostMapping("/by-id/{id}")
    public ResponseEntity<byte[]> generateById(@PathVariable long id,
                                               @RequestParam(name = "format", defaultValue = "JSON") String formatParam) {
        ReportFormat format = ReportFormat.fromParam(formatParam);
        RenderedReport report = reportGenerationService.generateById(id, format);
        return buildResponse(report);
    }

    @PostMapping("/by-package/{packageName}")
    public ResponseEntity<byte[]> generateByPackage(@PathVariable String packageName,
                                                    @RequestParam(name = "format", defaultValue = "JSON") String formatParam) {
        ReportFormat format = ReportFormat.fromParam(formatParam);
        RenderedReport report = reportGenerationService.generateByPackage(packageName, format);
        return buildResponse(report);
    }

    private ResponseEntity<byte[]> buildResponse(RenderedReport report) {
        MediaType mediaType = MediaType.parseMediaType(report.contentType());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(buildDisposition(report));
        return new ResponseEntity<>(report.content(), headers, HttpStatus.OK);
    }

    private ContentDisposition buildDisposition(RenderedReport report) {
        return ContentDisposition.attachment()
                .filename(report.filename())
                .build();
    }
}
