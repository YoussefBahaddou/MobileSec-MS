package com.mobilesec.reportgen.rendering;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilesec.reportgen.dto.ComprehensiveReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonReportRenderer implements ReportRenderer {

    private final ObjectMapper objectMapper;

    @Override
    public ReportFormat format() {
        return ReportFormat.JSON;
    }

    @Override
    public RenderedReport render(ComprehensiveReportDto data) {
        try {
            byte[] content = objectMapper.writeValueAsBytes(data);
            return new RenderedReport(content, "report.json", "application/json");
        } catch (Exception e) {
            throw new RuntimeException("Failed to render JSON report", e);
        }
    }
}
