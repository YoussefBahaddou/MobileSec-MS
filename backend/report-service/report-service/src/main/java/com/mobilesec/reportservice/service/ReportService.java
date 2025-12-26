package com.mobilesec.reportservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilesec.reportservice.dto.ComprehensiveReportDto;
import com.mobilesec.reportservice.entity.ReportEntity;
import com.mobilesec.reportservice.exception.AnalysisResultNotFoundException;
import com.mobilesec.reportservice.rendering.RenderedReport;
import com.mobilesec.reportservice.rendering.ReportFormat;
import com.mobilesec.reportservice.rendering.ReportRenderer;
import com.mobilesec.reportservice.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

            // Check if exists, update if needed
            reportRepository.findByScanId(dto.getScanId()).ifPresent(e -> entity.setId(e.getId()));

            return reportRepository.save(entity);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Concurrency handling: if duplicate key, return the existing one
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
                .orElseThrow(() -> new AnalysisResultNotFoundException("Report not found for scanId: " + scanId));

        return mapToDto(entity);
    }

    public RenderedReport generateReport(String scanId, ReportFormat format) {
        ComprehensiveReportDto dto = getReport(scanId);

        ReportRenderer renderer = renderers.stream()
                .filter(r -> r.format() == format)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported format: " + format));

        // Note: Renderers might need to be updated to accept ComprehensiveReportDto
        // For now, we assume they accept the DTO.
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
