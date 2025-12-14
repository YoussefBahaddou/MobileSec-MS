package com.mobilesec.reportservice.rendering;

import com.mobilesec.reportservice.dto.ComprehensiveReportDto;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;

@Component
public class PdfReportRenderer implements ReportRenderer {

  @Override
  public ReportFormat format() {
    return ReportFormat.PDF;
  }

  @Override
  public RenderedReport render(ComprehensiveReportDto result) {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      String html = buildHtml(result);

      PdfRendererBuilder builder = new PdfRendererBuilder();
      builder.useFastMode();
      builder.withHtmlContent(html, "");
      builder.toStream(os);
      builder.run();

      return new RenderedReport(os.toByteArray(), "report.pdf", "application/pdf");
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate PDF", e);
    }
  }

  private String buildHtml(ComprehensiveReportDto dto) {
    StringBuilder sb = new StringBuilder();
    sb.append("<html><head><style>");
    sb.append("body { font-family: Helvetica, Arial, sans-serif; color: #333; }");
    sb.append("h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }");
    sb.append("h2 { color: #e67e22; margin-top: 30px; }");
    sb.append(".header { background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin-bottom: 20px; }");
    sb.append(
        ".badge { background-color: #3498db; color: white; padding: 5px 10px; border-radius: 15px; font-size: 0.8em; }");
    sb.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; }");
    sb.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
    sb.append("th { background-color: #f2f2f2; }");
    sb.append(".risk-high { color: #e74c3c; font-weight: bold; }");
    sb.append(".risk-medium { color: #f39c12; font-weight: bold; }");
    sb.append("</style></head><body>");

    // Header
    sb.append("<div class='header'>");
    sb.append("<h1>Security Analysis Report</h1>");
    sb.append("<p><strong>Scan ID:</strong> ").append(dto.getScanId()).append("</p>");
    sb.append("<p><strong>Generated At:</strong> ").append(Instant.now()).append("</p>");
    if (dto.getManifest() != null) {
      sb.append("<p><strong>Package:</strong> ").append(dto.getManifest().getPackageName()).append("</p>");
      sb.append("<p><strong>Version:</strong> ").append(dto.getManifest().getVersionCode()).append("</p>");
    }
    sb.append("</div>");

    // Secrets Section
    sb.append("<h2>Hardcoded Secrets Findings</h2>");
    if (dto.getSecrets() != null && dto.getSecrets().getFindings() != null
        && !dto.getSecrets().getFindings().isEmpty()) {
      sb.append("<table><thead><tr><th>Type</th><th>Match Input</th></tr></thead><tbody>");
      for (ComprehensiveReportDto.SecretFinding finding : dto.getSecrets().getFindings()) {
        sb.append("<tr>");
        sb.append("<td><span class='risk-high'>").append(escape(finding.getType())).append("</span></td>");
        sb.append("<td>").append(escape(finding.getMatch())).append("</td>");
        sb.append("</tr>");
      }
      sb.append("</tbody></table>");
    } else {
      sb.append("<p>No secrets detected.</p>");
    }

    // Crypto Section
    sb.append("<h2>Cryptographic Issues</h2>");
    if (dto.getCrypto() != null && dto.getCrypto().getFindings() != null && !dto.getCrypto().getFindings().isEmpty()) {
      sb.append("<table><thead><tr><th>Rule ID</th><th>Description</th><th>Severity</th></tr></thead><tbody>");
      for (ComprehensiveReportDto.CryptoFinding finding : dto.getCrypto().getFindings()) {
        sb.append("<tr>");
        sb.append("<td>").append(escape(finding.getRuleId())).append("</td>");
        sb.append("<td>").append(escape(finding.getDescription())).append("</td>");
        sb.append("<td>").append(escape(finding.getSeverity())).append("</td>");
        sb.append("</tr>");
      }
      sb.append("</tbody></table>");
    } else {
      sb.append("<p>No cryptographic issues detected.</p>");
    }

    // Manifest Section
    sb.append("<h2>Manifest Configuration</h2>");
    if (dto.getManifest() != null) {
      sb.append("<ul>");
      sb.append("<li><strong>Debuggable:</strong> ")
          .append(dto.getManifest().isDebuggable() ? "<span class='risk-high'>YES (Risk)</span>" : "No")
          .append("</li>");
      sb.append("<li><strong>Allow Backup:</strong> ")
          .append(dto.getManifest().isAllowBackup() ? "<span class='risk-medium'>YES (Risk)</span>" : "No")
          .append("</li>");
      sb.append("<li><strong>Cleartext Traffic:</strong> ")
          .append(dto.getManifest().isUsesCleartextTraffic() ? "<span class='risk-high'>YES (Risk)</span>" : "No")
          .append("</li>");
      sb.append("</ul>");

      sb.append("<h3>Permissions</h3>");
      if (dto.getManifest().getPermissions() != null) {
        sb.append("<ul>");
        for (String perm : dto.getManifest().getPermissions()) {
          sb.append("<li>").append(escape(perm)).append("</li>");
        }
        sb.append("</ul>");
      }
    } else {
      sb.append("<p>No manifest info available.</p>");
    }

    sb.append("</body></html>");
    return sb.toString();
  }

  private String escape(String input) {
    if (input == null)
      return "";
    return input.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}
