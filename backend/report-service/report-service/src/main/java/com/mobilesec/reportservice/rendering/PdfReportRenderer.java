package com.mobilesec.reportservice.rendering;

import com.mobilesec.reportservice.dto.AnalysisResultDto;
import com.mobilesec.reportservice.dto.RiskLevel;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.ByteArrayOutputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PdfReportRenderer implements ReportRenderer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'")
            .withZone(ZoneOffset.UTC);

    @Override
    public ReportFormat format() {
        return ReportFormat.PDF;
    }

    @Override
    public RenderedReport render(AnalysisResultDto result) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            String htmlContent = buildHtml(result);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(baos);
            builder.run();

            String identifier = result.id() != null ? String.valueOf(result.id()) : safe(result.packageName());
            String filename = "analysis-report-" + (identifier == null ? "unknown" : identifier) + ".pdf";
            return new RenderedReport(baos.toByteArray(), format(), filename);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render PDF report", e);
        }
    }

    private String buildHtml(AnalysisResultDto result) {
        StringBuilder html = new StringBuilder();
        html.append("""
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE html>
                <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <title>MobileSec-MS Analysis Report</title>
                  <style type="text/css">
                    :root {
                      color-scheme: only light;
                    }
                    body {
                      font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
                      margin: 24px;
                      color: #1f2933;
                      line-height: 1.55;
                      background-color: #ffffff;
                    }
                    h1 {
                      font-size: 28px;
                      margin-bottom: 6px;
                      letter-spacing: 0.02em;
                    }
                    header p {
                      margin: 0;
                    }
                    h2 {
                      font-size: 20px;
                      margin-top: 28px;
                      border-bottom: 1px solid #d2d6dc;
                      padding-bottom: 6px;
                      text-transform: uppercase;
                      letter-spacing: 0.05em;
                      color: #0f172a;
                    }
                    p {
                      margin: 6px 0;
                    }
                    table {
                      width: 100%;
                      border-collapse: collapse;
                      margin-top: 12px;
                      font-size: 13px;
                    }
                    th, td {
                      border: 1px solid #d2d6dc;
                      padding: 8px 10px;
                      text-align: left;
                      vertical-align: top;
                    }
                    th {
                      background-color: #eef2f7;
                      font-weight: 600;
                    }
                    .badge {
                      display: inline-block;
                      padding: 4px 12px;
                      border-radius: 999px;
                      color: #ffffff;
                      font-weight: 600;
                      letter-spacing: 0.04em;
                    }
                    .badge-high { background-color: #c81e1e; }
                    .badge-medium { background-color: #f97316; }
                    .badge-low { background-color: #16a34a; }
                    .badge-unknown { background-color: #4b5563; }
                    .section {
                      margin-top: 28px;
                    }
                    .muted {
                      color: #61708a;
                    }
                    ul {
                      padding-left: 20px;
                      margin: 8px 0;
                    }
                    code {
                      background-color: #f3f4f6;
                      padding: 3px 6px;
                      border-radius: 4px;
                      font-size: 0.95em;
                    }
                    .summary-grid {
                      display: grid;
                      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
                      gap: 14px;
                      margin-top: 16px;
                    }
                    .summary-card {
                      border: 1px solid #d4d8e0;
                      border-radius: 12px;
                      padding: 14px 16px;
                      background-color: #f8fafc;
                      box-shadow: 0 1px 2px rgba(15, 23, 42, 0.08);
                    }
                    .summary-card h3 {
                      margin: 0 0 6px 0;
                      font-size: 12px;
                      text-transform: uppercase;
                      letter-spacing: 0.08em;
                      color: #64748b;
                    }
                    .summary-card p {
                      margin: 0;
                      font-size: 16px;
                      font-weight: 600;
                      color: #0f172a;
                    }
                    .divider {
                      height: 1px;
                      background: linear-gradient(90deg, rgba(203, 213, 225, 0.05) 0%, rgba(100, 116, 139, 0.6) 50%, rgba(203, 213, 225, 0.05) 100%);
                      margin: 32px 0;
                    }
                  </style>
                </head>
                <body>
                <header>
                  <h1>MobileSec-MS Analysis Report</h1>
                  <p class="muted">Generated by report-service</p>
                </header>
                """);

        html.append("""
                <section class="section">
                  <h2>Summary</h2>
                  <div class="summary-grid">
                    <div class="summary-card">
                      <h3>Package</h3>
                      <p>%s</p>
                    </div>
                    <div class="summary-card">
                      <h3>Version</h3>
                      <p>%s</p>
                    </div>
                    <div class="summary-card">
                      <h3>Generated</h3>
                      <p>%s</p>
                    </div>
                    <div class="summary-card">
                      <h3>Risk Level</h3>
                      <p><span class="badge %s">%s</span></p>
                    </div>
                  </div>
                </section>
                """
                .formatted(
                        safe(result.packageName()),
                        safe(result.versionName()),
                        result.createdAt() != null ? DATE_FORMATTER.format(result.createdAt()) : "N/A",
                        cssClassForRisk(result.riskLevel()),
                        result.riskLevel() != null ? result.riskLevel().name() : RiskLevel.UNKNOWN.name()
                ));

        html.append("""
                <div class="divider"></div>
                <section class="section">
                  <h2>Risk Highlights</h2>
                """);

        List<String> riskReasons = result.riskReasons();
        if (riskReasons == null || riskReasons.isEmpty()) {
            html.append("<p class=\"muted\">No explicit risk reasons were provided.</p>");
        } else {
            html.append("<ul>");
            for (String reason : riskReasons) {
                html.append("<li>").append(safe(reason)).append("</li>");
            }
            html.append("</ul>");
        }
        html.append("</section>");

        html.append("<div class=\"divider\"></div>");

        html.append("<section class=\"section\"><h2>Manifest Flags</h2><table><tbody>");
        AnalysisResultDto.ManifestFlags flags = result.manifestFlags();
        if (flags != null) {
            appendFlagRow(html, "Debuggable", flags.debuggable());
            appendFlagRow(html, "Allow Backup", flags.allowBackup());
            appendFlagRow(html, "Cleartext Traffic Permitted", flags.cleartextTrafficPermitted());
        } else {
            html.append("<tr><td colspan=\"2\" class=\"muted\">No manifest flag data was provided.</td></tr>");
        }
        html.append("</tbody></table></section>");

        html.append("<div class=\"divider\"></div>");

        html.append("<section class=\"section\"><h2>Declared Permissions</h2>");
        List<String> permissions = result.permissions();
        if (permissions == null || permissions.isEmpty()) {
            html.append("<p class=\"muted\">No permissions were returned by analysis-service.</p>");
        } else {
            html.append("<table><thead><tr><th>#</th><th>Permission</th></tr></thead><tbody>");
            for (int i = 0; i < permissions.size(); i++) {
                html.append("<tr><td>").append(i + 1).append("</td><td><code>")
                        .append(safe(permissions.get(i))).append("</code></td></tr>");
            }
            html.append("</tbody></table>");
        }
        html.append("</section>");

        html.append("<div class=\"divider\"></div>");

        html.append("<section class=\"section\"><h2>Exported Components</h2>");
        List<AnalysisResultDto.ExportedComponent> components = result.exportedComponents();
        if (components == null || components.isEmpty()) {
            html.append("<p class=\"muted\">No exported component information provided.</p>");
        } else {
            html.append("<table><thead><tr><th>Name</th><th>Type</th><th>Exported</th><th>Permission</th><th>Intent Filters</th></tr></thead><tbody>");
            for (AnalysisResultDto.ExportedComponent component : components) {
                html.append("<tr><td>").append(safe(component.name())).append("</td>")
                        .append("<td>").append(safe(component.type())).append("</td>")
                        .append("<td>").append(component.exported() ? "Yes" : "No").append("</td>")
                        .append("<td>").append(safe(component.permission())).append("</td>")
                        .append("<td>").append(safe(String.join(", ", component.intentFilters() == null ? List.of() : component.intentFilters())))
                        .append("</td></tr>");
            }
            html.append("</tbody></table>");
        }
        html.append("</section>");

        html.append("</body>\n</html>");
        return html.toString();
    }

    private void appendFlagRow(StringBuilder html, String label, boolean value) {
        html.append("<tr><th>").append(safe(label)).append("</th><td>")
                .append(value ? "Enabled" : "Disabled")
                .append("</td></tr>");
    }

    private String cssClassForRisk(RiskLevel riskLevel) {
        RiskLevel level = riskLevel == null ? RiskLevel.UNKNOWN : riskLevel;
        return switch (level) {
            case CRITICAL, HIGH -> "badge-high";
            case MEDIUM -> "badge-medium";
            case LOW -> "badge-low";
            case UNKNOWN -> "badge-unknown";
        };
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "<span class=\"muted\">N/A</span>";
        }
        return HtmlUtils.htmlEscape(value);
    }
}
