package com.mobilsec.analysis.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilsec.analysis.persistence.AnalysisResult;
import com.mobilsec.analysis.persistence.AnalysisResultRepository;
import com.mobilsec.analysis.web.dto.AnalysisResultDto;

@Service
public class ApkAnalysisService {

    private final ApkMetadataExtractor extractor;
    private final AnalysisResultRepository repository;
    private final ObjectMapper objectMapper;
    private final RiskScorer riskScorer;
    private final AnalysisResultMapper mapper;

    public ApkAnalysisService(ApkMetadataExtractor extractor,
            AnalysisResultRepository repository,
            ObjectMapper objectMapper,
            RiskScorer riskScorer,
            AnalysisResultMapper mapper) {
        this.extractor = extractor;
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.riskScorer = riskScorer;
        this.mapper = mapper;
    }

    public AnalysisResultDto analyze(MultipartFile file) throws IOException {
        Path tempFile = Files.createTempFile("apk-upload-", ".apk");
        try {
            file.transferTo(tempFile);
            ApkMetadata metadata = extractor.extract(tempFile);
            RiskAssessment riskAssessment = riskScorer.score(metadata);

            Instant now = Instant.now();
            AnalysisResult entity = new AnalysisResult(
                    metadata.packageName(),
                    metadata.versionName(),
                    writeValue(metadata.permissions()),
                    writeValue(metadata.manifestFlags()),
                    writeValue(metadata.exportedComponents()),
                    writeValue(riskAssessment.reasons()),
                    riskAssessment.level().name(),
                    now,
                    writeValue(metadata.secrets()),
                    writeValue(metadata.cryptoIssues()));

            entity = repository.save(entity);

            return mapper.fromEntity(entity);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private String writeValue(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }
}
