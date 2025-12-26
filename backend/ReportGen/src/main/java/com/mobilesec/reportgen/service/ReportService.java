package com.mobilesec.reportgen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilesec.reportgen.dto.ComprehensiveReportDto;
import com.mobilesec.reportgen.entity.ReportEntity;
import com.mobilesec.reportgen.rendering.RenderedReport;
import com.mobilesec.reportgen.rendering.ReportFormat;
import com.mobilesec.reportgen.rendering.ReportRenderer;
import com.mobilesec.reportgen.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final ObjectMapper objectMapper;
    private final List<ReportRenderer> renderers;

    public ReportEntity saveReport(ComprehensiveReportDto dto) {
        try {
            ReportEntity entity = ReportEntity.builder()
                    .scanId(dto.getScanId())
                    .packageName(dto.getManifest() != null ? dto.getManifest().getPackageName() : "Unknown")
                    .manifestResultJson(objectMapper.writeValueAsString(dto.getManifest()))
                    .secretResultJson(objectMapper.writeValueAsString(dto.getSecrets()))
                    .cryptoResultJson(objectMapper.writeValueAsString(dto.getCrypto()))
                    .riskScore(calculateScore(dto))
                    .build();

            // Check if exists, update if needed (optional, here we rely on DB unique
            // constraint mostly)
            reportRepository.findByScanId(dto.getScanId()).ifPresent(e -> entity.setId(e.getId()));

            return reportRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            log.warn("Report with scanId {} already exists, retrieving existing record.", dto.getScanId());
            return reportRepository.findByScanId(dto.getScanId())
                    .orElseThrow(() -> new RuntimeException(
                            "Failed to retrieve existing report after constraint violation", e));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing report data", e);
        }
    }

    public ComprehensiveReportDto getReport(String scanId) {
        ReportEntity entity = reportRepository.findByScanId(scanId)
                .orElseThrow(() -> new RuntimeException("Report not found for scanId: " + scanId));

        return mapToDto(entity);
    }

    public RenderedReport generateReport(String scanId, ReportFormat format) {
        ComprehensiveReportDto dto = getReport(scanId);

        ReportRenderer renderer = renderers.stream()
                .filter(r -> r.format() == format)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported format: " + format));

        return renderer.render(dto);
    }

    private ComprehensiveReportDto mapToDto(ReportEntity entity) {
        try {
            return ComprehensiveReportDto.builder()
                    .scanId(entity.getScanId())
                    .manifest(objectMapper.readValue(entity.getManifestResultJson(),
                            ComprehensiveReportDto.ManifestResult.class))
                    .secrets(objectMapper.readValue(entity.getSecretResultJson(),
                            ComprehensiveReportDto.SecretResult.class))
                    .crypto(objectMapper.readValue(entity.getCryptoResultJson(),
                            ComprehensiveReportDto.CryptoResult.class))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing report data", e);
        }
    }

    private int calculateScore(ComprehensiveReportDto dto) {
        int score = 100;
        if (dto.getManifest() != null) {
            if (dto.getManifest().isDebuggable())
                score -= 20;
            if (dto.getManifest().isAllowBackup())
                score -= 10;
            if (dto.getManifest().isUsesCleartextTraffic())
                score -= 20;
        }
        if (dto.getSecrets() != null && dto.getSecrets().getFindings() != null) {
            score -= (dto.getSecrets().getFindings().size() * 30);
        }
        if (dto.getCrypto() != null && dto.getCrypto().getFindings() != null) {
            score -= (dto.getCrypto().getFindings().size() * 10);
        }
        return Math.max(0, score);
    }
}
