package com.mobilesec.reportgen.rendering;

import com.mobilesec.reportgen.dto.ComprehensiveReportDto;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class PdfReportRenderer implements ReportRenderer {

    private static final Logger logger = LoggerFactory.getLogger(PdfReportRenderer.class);

    @Override
    public ReportFormat format() {
        return ReportFormat.PDF;
    }

    @Override
    public RenderedReport render(ComprehensiveReportDto data) {
        String html = "";
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            html = buildHtml(data);

            // Log HTML for debugging if parsing fails
            logger.debug("Generating PDF for ScanId: {}. HTML Length: {}", data.getScanId(), html.length());

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useSVGDrawer(new BatikSVGDrawer());
            builder.withHtmlContent(html, "");
            builder.toStream(os);
            builder.run();

            String filename = "security_report_" + data.getScanId().substring(0, 8) + ".pdf";
            return new RenderedReport(os.toByteArray(), filename, "application/pdf");
        } catch (Exception e) {
            logger.error("Failed to render PDF for ScanId: {}", data.getScanId());

            // Advanced Debug Logging for XML Parse Errors
            if (e.getMessage() != null && e.getMessage().contains("lineNumber: 1")) {
                try {
                    int errCol = 3781; // Based on observed error logs, can be regex extracted later
                    if (html.length() > errCol) {
                        int start = Math.max(0, errCol - 50);
                        int end = Math.min(html.length(), errCol + 50);
                        logger.error("Error Context [Col {}]: '{}'", errCol, html.substring(start, end));
                        logger.error("Char at {}: '{}'", errCol, html.charAt(errCol)); // Is it '&'?
                    }
                } catch (Exception ignore) {
                }
            }
            // Log snippet
            logger.error("Offending HTML (first 5000 chars): {}", truncate(html, 5000));
            throw new RuntimeException("Failed to render PDF", e);
        }
    }

    private String buildHtml(ComprehensiveReportDto data) {
        // --- 1. STATISTICS CALCULATION ---
        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int safeCount = 0;

        if (data.getSecrets() != null && data.getSecrets().getFindings() != null) {
            criticalCount += data.getSecrets().getFindings().size();
        }

        if (data.getCrypto() != null && data.getCrypto().getFindings() != null) {
            for (var f : data.getCrypto().getFindings()) {
                if ("CRITICAL".equalsIgnoreCase(f.getSeverity()))
                    criticalCount++;
                else if ("HIGH".equalsIgnoreCase(f.getSeverity()))
                    highCount++;
                else
                    mediumCount++;
            }
        }

        if (data.getManifest() != null) {
            if (data.getManifest().isDebuggable())
                highCount++;
            else
                safeCount++;
            if (data.getManifest().isAllowBackup())
                mediumCount++;
            else
                safeCount++;
            if (data.getManifest().isUsesCleartextTraffic())
                highCount++;
            else
                safeCount++;
        }

        int totalFound = criticalCount + highCount + mediumCount;
        int score = Math.max(0, 100 - (criticalCount * 20) - (highCount * 10) - (mediumCount * 5));

        // --- 2. HTML GENERATION ---
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><style>");

        sb.append("@page { size: A4; margin: 25mm 20mm; ");
        sb.append(
                " @bottom-left { content: 'MobileSec Automated Assessment'; font-family: Helvetica, sans-serif; font-size: 9px; color: #95a5a6; }");
        sb.append(
                " @bottom-right { content: 'Page ' counter(page) ' of ' counter(pages) ' | ' 'Copyright © 2025 MobileSec Inc.'; font-family: Helvetica, sans-serif; font-size: 9px; color: #95a5a6; }");
        sb.append("}");

        sb.append("body { font-family: 'Helvetica', sans-serif; color: #2c3e50; line-height: 1.5; font-size: 11px; }");
        sb.append("h1 { font-size: 26px; color: #2c3e50; margin-bottom: 5px; font-weight: 700; }");
        sb.append(
                "h2 { font-size: 18px; color: #34495e; border-bottom: 2px solid #3498db; padding-bottom: 5px; margin-top: 30px; margin-bottom: 15px;}");
        sb.append(
                "h3 { font-size: 14px; color: #7f8c8d; margin-top: 20px; text-transform: uppercase; letter-spacing: 0.5px; }");

        sb.append(".header { text-align: center; margin-bottom: 40px; }");
        sb.append(".meta { color: #7f8c8d; font-size: 12px; margin-top: 5px; }");
        sb.append(".logo { width: 60px; height: 60px; margin-bottom: 10px; }");

        sb.append(".grid { display: table; width: 100%; border-spacing: 20px 0; }");
        sb.append(".col { display: table-cell; vertical-align: top; width: 50%; }");

        sb.append(
                ".badge { padding: 4px 8px; border-radius: 4px; font-weight: bold; color: white; font-size: 10px; display: inline-block; }");
        sb.append(
                ".crt { background-color: #c0392b; } .hgh { background-color: #e74c3c; } .med { background-color: #f39c12; } .saf { background-color: #27ae60; }");

        sb.append(
                ".code { font-family: 'Courier New', monospace; background: #ecf0f1; padding: 6px; border-radius: 4px; border: 1px solid #bdc3c7; font-size: 10px; word-wrap: break-word; }");

        sb.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; }");
        sb.append("th { background-color: #34495e; color: white; padding: 10px; text-align: left; font-size: 10px; }");
        sb.append("td { border-bottom: 1px solid #bdc3c7; padding: 8px; font-size: 10px; vertical-align: top; }");
        sb.append("tr:nth-child(even) { background-color: #f8f9fa; }");

        sb.append("</style></head><body>");

        // HEADER & LOGO
        sb.append("<div class='header'>");
        sb.append(
                "<svg class='logo' width='60' height='60' viewBox='0 0 24 24' xmlns='http://www.w3.org/2000/svg'><path fill='#3498db' d='M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z'/></svg>");
        sb.append("<h1>Security Assessment Report</h1>");
        sb.append("<div class='meta'>CONFIDENTIAL DOCUMENT | Generated by MobileSec</div>");
        sb.append("<div class='meta'>")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US)))
                .append(" | Scan ID: ").append(escape(data.getScanId())).append("</div>");
        sb.append("</div>");

        // SUMMARY SECTION (Charts)
        sb.append("<h2>Executive Summary</h2>");
        sb.append("<div class='grid'>");

        sb.append("<div class='col' style='text-align:center;'>");
        sb.append("<h3>Security Score</h3>");
        sb.append(generateDonutChart(score));
        sb.append("<p style='font-size:16px; font-weight:bold; margin-top:10px;'>").append(score).append("/100</p>");
        sb.append("</div>");

        sb.append("<div class='col'>");
        sb.append("<h3>Findings Distribution</h3>");
        sb.append(generateBarChart(criticalCount, highCount, mediumCount, safeCount));
        sb.append("</div>");

        sb.append("</div>");

        // ML RISK ASSESSMENT
        if (data.getManifest() != null && data.getManifest().getSecurityScore() != null) {
            var ss = data.getManifest().getSecurityScore();
            sb.append("<h3>ML Risk Assessment</h3>");
            sb.append("<div style='background: #f8f9fa; padding: 15px; border-radius: 5px; border-left: 5px solid #");
            // Choose color
            String color = "#27ae60"; // Safe
            if (ss.getScore() > 70)
                color = "#e74c3c"; // Critical
            else if (ss.getScore() > 30)
                color = "#f39c12"; // Suspicious

            sb.append(color.substring(1)).append(";'>");

            sb.append("<div style='font-size: 20px; font-weight: bold; color: ").append(color).append(";'>")
                    .append(ss.getScore()).append("% ").append(escape(ss.getRiskLabel())).append("</div>");
            sb.append("<div style='margin-top: 5px; color: #7f8c8d;'>").append(escape(ss.getDetails()))
                    .append("</div>");
            sb.append("</div>");
            sb.append("<br/>");
        }

        // APP DETAILS
        if (data.getManifest() != null) {
            sb.append("<h3>Target Application</h3>");
            sb.append("<table><tr><th>Package Name</th><th>Version Code</th><th>Total Issues</th></tr>");
            sb.append("<tr>");
            sb.append("<td>").append(escape(data.getManifest().getPackageName())).append("</td>");
            sb.append("<td>").append(escape(data.getManifest().getVersionCode())).append("</td>");
            sb.append("<td>").append(totalFound).append("</td>");
            sb.append("</tr></table>");
        }

        // 1. MANIFEST
        sb.append("<h2>1. Manifest &amp; Permissions</h2>");
        if (data.getManifest() != null) {
            sb.append(
                    "<table><thead><tr><th width='15%'>Status</th><th>Check</th><th>Risk Description</th></tr></thead><tbody>");
            renderManifestRow(sb, "Debuggable", data.getManifest().isDebuggable(), "App is debuggable. Critical risk.",
                    true);
            renderManifestRow(sb, "Allow Backup", data.getManifest().isAllowBackup(),
                    "App data can be extracted via ADB backup.", false);
            renderManifestRow(sb, "Cleartext Traffic", data.getManifest().isUsesCleartextTraffic(),
                    "Unencrypted network traffic allowed.", true);
            sb.append("</tbody></table>");

            if (data.getManifest().getPermissions() != null) {
                sb.append("<h3>Requested Permissions</h3><div class='code'>");
                for (String p : data.getManifest().getPermissions())
                    // Replace & with _ to handle edge case permissions
                    sb.append(escape(p).replace("&amp;", "_")).append(", ");
                sb.append("</div>");
            }
        }

        // 2. SECRETS
        sb.append("<h2>2. Hardcoded Secrets</h2>");
        if (data.getSecrets() != null && data.getSecrets().getFindings() != null
                && !data.getSecrets().getFindings().isEmpty()) {
            sb.append(
                    "<table><thead><tr><th width='15%'>Severity</th><th width='20%'>Type</th><th>Match Preview</th></tr></thead><tbody>");
            for (var f : data.getSecrets().getFindings()) {
                sb.append("<tr>");
                sb.append("<td><span class='badge crt'>CRITICAL</span></td>");
                sb.append("<td>").append(escape(f.getType())).append("</td>");
                sb.append("<td><div class='code'>").append(escape(truncate(f.getMatch(), 60))).append("</div></td>");
                sb.append("</tr>");
            }
            sb.append("</tbody></table>");
        } else {
            sb.append("<p class='badge saf'>No Secrets Found</p>");
        }

        // 3. CRYPTO
        sb.append("<h2>3. Cryptography Analysis</h2>");
        if (data.getCrypto() != null && data.getCrypto().getFindings() != null
                && !data.getCrypto().getFindings().isEmpty()) {
            sb.append(
                    "<table><thead><tr><th width='15%'>Severity</th><th>Rule</th><th>Location</th></tr></thead><tbody>");
            for (var f : data.getCrypto().getFindings()) {
                String cls = "med";
                if ("CRITICAL".equalsIgnoreCase(f.getSeverity()))
                    cls = "crt";
                if ("HIGH".equalsIgnoreCase(f.getSeverity()))
                    cls = "hgh";

                sb.append("<tr>");
                sb.append("<td><span class='badge ").append(cls).append("'>").append(escape(f.getSeverity()))
                        .append("</span></td>");
                sb.append("<td><strong>").append(escape(f.getRuleId())).append("</strong><br/>")
                        .append(escape(f.getDescription())).append("</td>");
                sb.append("<td>").append(escape(f.getFileName())).append(":").append(f.getLineNumber()).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</tbody></table>");
        } else {
            sb.append("<p class='badge saf'>No Cryptography Issues Found</p>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    private void renderManifestRow(StringBuilder sb, String name, boolean isRisk, String desc, boolean isHigh) {
        sb.append("<tr>");
        if (isRisk) {
            sb.append("<td><span class='badge ").append(isHigh ? "hgh" : "med").append("'>FAIL</span></td>");
        } else {
            sb.append("<td><span class='badge saf'>PASS</span></td>");
        }
        sb.append("<td><strong>").append(name).append("</strong></td>");
        sb.append("<td>").append(isRisk ? desc : "Check passed.").append("</td>");
        sb.append("</tr>");
    }

    private String generateDonutChart(int score) {
        String color = score > 80 ? "#27ae60" : (score > 50 ? "#f39c12" : "#c0392b");
        double dashArray = (score / 100.0) * 251.2;

        return String.format(Locale.US,
                "<svg width='120' height='120' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'>" +
                        "<circle cx='50' cy='50' r='40' stroke='#ecf0f1' stroke-width='10' fill='none' />" +
                        "<circle cx='50' cy='50' r='40' stroke='%s' stroke-width='10' fill='none' " +
                        "stroke-dasharray='%f 251.2' transform='rotate(-90 50 50)' />" +
                        "<text x='50' y='55' font-family='Helvetica' font-size='20' font-weight='bold' text-anchor='middle' fill='#2c3e50'>%d</text>"
                        +
                        "</svg>",
                color, dashArray, score);
    }

    private String generateBarChart(int crit, int high, int med, int safe) {
        int max = Math.max(1, Math.max(Math.max(crit, high), Math.max(med, safe)));

        StringBuilder svg = new StringBuilder();
        svg.append(
                "<svg width='500' height='120' viewBox='0 0 300 120' font-family='Helvetica' font-size='10' xmlns='http://www.w3.org/2000/svg'>");
        svg.append(drawBar(0, "Critical", crit, max, "#c0392b"));
        svg.append(drawBar(30, "High", high, max, "#e74c3c"));
        svg.append(drawBar(60, "Medium", med, max, "#f39c12"));
        svg.append(drawBar(90, "Safe", safe, max, "#27ae60"));
        svg.append("</svg>");
        return svg.toString();
    }

    private String drawBar(int y, String label, int val, int max, String color) {
        int barWidth = (int) ((val / (double) max) * 200);
        return String.format(Locale.US,
                "<text x='0' y='%d' fill='#7f8c8d'>%s (%d)</text>" +
                        "<rect x='70' y='%d' width='%d' height='15' fill='%s' rx='3' />",
                y + 12, label, val, y + 2, Math.max(5, barWidth), color);
    }

    private String escape(String input) {
        if (input == null)
            return "";
        // 1. Strip control characters which are illegal in XML 1.0 (except tab, cr, lf)
        String cleaned = input.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        // 2. Standard XML escaping
        return cleaned.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String truncate(String input, int max) {
        if (input == null)
            return "";
        if (input.length() <= max)
            return input;
        return input.substring(0, max) + "...";
    }
}
