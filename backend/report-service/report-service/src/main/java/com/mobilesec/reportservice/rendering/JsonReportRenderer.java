package com.mobilesec.reportservice.rendering;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilesec.reportservice.dto.AnalysisResultDto;
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
    public RenderedReport render(AnalysisResultDto result) {
        try {
            byte[] content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(result);
            String filename = ReportFilenameUtils.composeFilename(result, ".json");
            return new RenderedReport(content, format(), filename);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize analysis result to JSON", e);
        }
    }
}
