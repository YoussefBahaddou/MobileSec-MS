package com.mobilesec.reportservice.rendering;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilesec.reportservice.dto.ComprehensiveReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SarifReportRenderer implements ReportRenderer {

    private final ObjectMapper objectMapper;

    @Override
    public ReportFormat format() {
        return ReportFormat.SARIF;
    }

    @Override
    public RenderedReport render(ComprehensiveReportDto result) {
        Map<String, Object> sarif = new HashMap<>();
        sarif.put("version", "2.1.0");
        sarif.put("$schema", "https://json.schemastore.org/sarif-2.1.0.json");

        List<Map<String, Object>> runs = new ArrayList<>();
        Map<String, Object> run = new HashMap<>();

        Map<String, Object> tool = new HashMap<>();
        Map<String, Object> driver = new HashMap<>();
        driver.put("name", "MobileSec Scanner");
        tool.put("driver", driver);
        run.put("tool", tool);

        List<Map<String, Object>> results = new ArrayList<>();

        // Convert Findings to SARIF
        if (result.getSecrets() != null && result.getSecrets().getFindings() != null) {
            for (ComprehensiveReportDto.SecretFinding f : result.getSecrets().getFindings()) {
                results.add(createResult("SECRET_LEAK", "Critical", f.getType() + ": " + f.getMatch()));
            }
        }

        if (result.getCrypto() != null && result.getCrypto().getFindings() != null) {
            for (ComprehensiveReportDto.CryptoFinding f : result.getCrypto().getFindings()) {
                results.add(createResult(f.getRuleId(), f.getSeverity(), f.getDescription()));
            }
        }

        run.put("results", results);
        runs.add(run);
        sarif.put("runs", runs);

        try {
            byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(sarif);
            return new RenderedReport(bytes, "report.sarif", "application/json");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to generate SARIF", e);
        }
    }

    private Map<String, Object> createResult(String ruleId, String level, String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("ruleId", ruleId);
        res.put("level", mapLevel(level));

        Map<String, Object> msg = new HashMap<>();
        msg.put("text", message);
        res.put("message", msg);

        return res;
    }

    private String mapLevel(String severity) {
        if (severity == null)
            return "warning";
        switch (severity.toLowerCase()) {
            case "critical":
                return "error";
            case "high":
                return "error";
            case "medium":
                return "warning";
            default:
                return "note";
        }
    }
}
