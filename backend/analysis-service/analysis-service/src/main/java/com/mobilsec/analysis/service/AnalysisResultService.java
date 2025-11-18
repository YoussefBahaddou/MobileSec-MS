package com.mobilsec.analysis.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilsec.analysis.persistence.AnalysisResult;
import com.mobilsec.analysis.persistence.AnalysisResultRepository;
import com.mobilsec.analysis.service.ManifestFlags;
import com.mobilsec.analysis.service.ExportedComponent;
import com.mobilsec.analysis.web.dto.AnalysisDetailDto;
import com.mobilsec.analysis.web.dto.AnalysisSummaryDto;

@Service
public class AnalysisResultService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final AnalysisResultRepository repository;
    private final ObjectMapper objectMapper;

    public AnalysisResultService(AnalysisResultRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public Page<AnalysisSummaryDto> list(int page, int size) {
        validatePaging(page, size);
        Pageable pageable = PageRequest.of(page, size, DEFAULT_SORT);
        Page<AnalysisResult> results = repository.findAll(pageable);

        List<AnalysisSummaryDto> summaries = results.getContent().stream()
                .map(this::toSummary)
                .toList();

        return new PageImpl<>(summaries, pageable, results.getTotalElements());
    }

    public AnalysisDetailDto getById(Long id) {
        AnalysisResult result = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis result not found"));
        return toDetail(result);
    }

    public AnalysisDetailDto getLatestByPackage(String packageName) {
        AnalysisResult result = repository.findTopByPackageNameOrderByCreatedAtDesc(packageName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not analysed"));
        return toDetail(result);
    }

    private void validatePaging(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination parameters");
        }
    }

    private AnalysisSummaryDto toSummary(AnalysisResult entity) {
        return new AnalysisSummaryDto(
                entity.getId(),
                entity.getPackageName(),
                entity.getRiskLevel(),
                entity.getCreatedAt());
    }

    private AnalysisDetailDto toDetail(AnalysisResult entity) {
        try {
            List<String> permissions = readValue(entity.getPermissionsJson(), new TypeReference<List<String>>() {
            });
            ManifestFlags manifestFlags = readValue(entity.getManifestFlagsJson(), ManifestFlags.class);
            List<ExportedComponent> exportedComponents = readValue(entity.getExportedComponentsJson(),
                    new TypeReference<List<ExportedComponent>>() {
                    });

            return new AnalysisDetailDto(
                    entity.getId(),
                    entity.getPackageName(),
                    entity.getVersionName(),
                    entity.getRiskLevel(),
                    entity.getCreatedAt(),
                    permissions,
                    manifestFlags,
                    exportedComponents);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read stored metadata", ex);
        }
    }

    private <T> T readValue(String json, Class<T> type) throws IOException {
        if (json == null) {
            return null;
        }
        return objectMapper.readValue(json, type);
    }

    private <T> T readValue(String json, TypeReference<T> type) throws IOException {
        if (json == null) {
            return null;
        }
        return objectMapper.readValue(json, type);
    }
}
