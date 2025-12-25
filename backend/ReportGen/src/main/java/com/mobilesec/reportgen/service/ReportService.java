package com.mobilesec.reportgen.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.mobilesec.reportgen.dto.ComprehensiveReportDto;
import com.mobilesec.reportgen.exception.AnalysisResultNotFoundException;
import com.mobilesec.reportgen.rendering.RenderedReport;
import com.mobilesec.reportgen.rendering.ReportFormat;
import com.mobilesec.reportgen.rendering.ReportRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class ReportService {

    private final Firestore firestore;
    private final ObjectMapper objectMapper;
    private final List<ReportRenderer> renderers;
    private final String collectionName;

    public ReportService(
            Firestore firestore,
            ObjectMapper objectMapper,
            List<ReportRenderer> renderers,
            @Value("${firestore.collection:apk_reports}") String collectionName
    ) {
        this.firestore = firestore;
        this.objectMapper = objectMapper;
        this.renderers = renderers;
        this.collectionName = collectionName;
    }

    public void saveReport(ComprehensiveReportDto dto) {
        try {
            Map<String, Object> payload = objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {
            });
            payload.put("riskScore", calculateScore(dto));
            payload.put("createdAt", Timestamp.now());

            DocumentReference document = firestore.collection(collectionName).document(dto.getScanId());
            ApiFuture<WriteResult> result = document.set(payload, SetOptions.merge());
            result.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while saving report to Firestore", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to persist report to Firestore", e);
        }
    }

    public ComprehensiveReportDto getReport(String scanId) {
        try {
            DocumentReference document = firestore.collection(collectionName).document(scanId);
            DocumentSnapshot snapshot = document.get().get();
            if (!snapshot.exists()) {
                throw new AnalysisResultNotFoundException("Report not found for scanId: " + scanId);
            }

            Map<String, Object> data = snapshot.getData();
            if (data == null) {
                throw new AnalysisResultNotFoundException("Report data missing for scanId: " + scanId);
            }

            // Remove Firestore bookkeeping fields before converting
            data = new HashMap<>(data);
            data.remove("createdAt");
            data.remove("riskScore");

            return objectMapper.convertValue(data, ComprehensiveReportDto.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while retrieving report from Firestore", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to load report from Firestore", e);
        }
    }

    public RenderedReport generateReport(String scanId, ReportFormat format) {
        ComprehensiveReportDto dto = getReport(scanId);

        ReportRenderer renderer = renderers.stream()
                .filter(r -> r.format() == format)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported format: " + format));

        return renderer.render(dto);
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
