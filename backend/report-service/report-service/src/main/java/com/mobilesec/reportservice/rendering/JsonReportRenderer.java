package com.mobilesec.reportservice.rendering;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilesec.reportservice.dto.ComprehensiveReportDto;
import org.springframework.stereotype.Component;

@Component
public class JsonReportRenderer implements ReportRenderer {

    private final ObjectMapper objectMapper;

    public JsonReportRenderer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ReportFormat format() {
        return ReportFormat.JSON;
    }

    @Override
    public RenderedReport render(ComprehensiveReportDto result) {
        try {
            byte[] content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(result);
            String filename = "report-" + result.getScanId() + ".json";
            return new RenderedReport(content, filename, "application/json");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize analysis result to JSON", e);
        }
    }
}
