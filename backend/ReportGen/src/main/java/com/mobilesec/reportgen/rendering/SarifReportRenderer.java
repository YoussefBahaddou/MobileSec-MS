package com.mobilesec.reportgen.rendering;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mobilesec.reportgen.dto.ComprehensiveReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class SarifReportRenderer implements ReportRenderer {

    private final ObjectMapper objectMapper;

    @Override
    public ReportFormat format() {
        return ReportFormat.SARIF;
    }

    @Override
    public RenderedReport render(ComprehensiveReportDto data) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("version", "2.1.0");
        root.put("$schema", "https://json.schemastore.org/sarif-2.1.0.json");

        ArrayNode runs = root.putArray("runs");
        ObjectNode run = runs.addObject();

        ObjectNode tool = run.putObject("tool");
        tool.putObject("driver").put("name", "MobileSec ReportGen");

        ArrayNode results = run.putArray("results");

        // Map Secrets
        if (data.getSecrets() != null && data.getSecrets().getFindings() != null) {
            for (var finding : data.getSecrets().getFindings()) {
                ObjectNode result = results.addObject();
                result.put("ruleId", "SECRET-" + finding.getType());
                result.put("level", "error");
                result.putObject("message").put("text", "Found secret: " + finding.getType());
            }
        }

        // Map Crypto
        if (data.getCrypto() != null && data.getCrypto().getFindings() != null) {
            for (var finding : data.getCrypto().getFindings()) {
                ObjectNode result = results.addObject();
                result.put("ruleId", finding.getRuleId());
                result.put("level", "warning");
                result.putObject("message").put("text", finding.getDescription());

                ObjectNode location = result.putArray("locations").addObject();
                ObjectNode physicalLocation = location.putObject("physicalLocation");
                physicalLocation.putObject("artifactLocation").put("uri", finding.getFileName());
                physicalLocation.putObject("region").put("startLine", finding.getLineNumber());
            }
        }

        try {
            byte[] content = objectMapper.writeValueAsBytes(root);
            return new RenderedReport(content, "report.sarif", "application/json");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate SARIF", e);
        }
    }
}
