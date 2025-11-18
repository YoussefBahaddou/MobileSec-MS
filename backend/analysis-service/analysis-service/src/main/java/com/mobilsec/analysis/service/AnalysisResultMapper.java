package com.mobilsec.analysis.service;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobilsec.analysis.persistence.AnalysisResult;
import com.mobilsec.analysis.web.dto.AnalysisResultDto;

@Component
public class AnalysisResultMapper {

    private static final ManifestFlags DEFAULT_FLAGS = new ManifestFlags(false, true, false);

    private final ObjectMapper objectMapper;

    public AnalysisResultMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AnalysisResultDto fromEntity(AnalysisResult entity) {
        try {
            List<String> permissions = readList(entity.getPermissionsJson(), new TypeReference<List<String>>() {
            });
            ManifestFlags manifestFlags = readValue(entity.getManifestFlagsJson(), ManifestFlags.class);
            List<ExportedComponent> exportedComponents = readList(entity.getExportedComponentsJson(),
                    new TypeReference<List<ExportedComponent>>() {
                    });
            List<String> riskReasons = readList(entity.getRiskReasonsJson(), new TypeReference<List<String>>() {
            });

            return new AnalysisResultDto(
                    entity.getId(),
                    entity.getPackageName(),
                    entity.getVersionName(),
                    entity.getRiskLevel(),
                    riskReasons,
                    permissions,
                    manifestFlags != null ? manifestFlags : DEFAULT_FLAGS,
                    exportedComponents,
                    entity.getCreatedAt());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read stored metadata", ex);
        }
    }

    private <T> T readValue(String json, Class<T> type) throws IOException {
        if (json == null || json.isBlank()) {
            return null;
        }
        return objectMapper.readValue(json, type);
    }

    private <T> List<T> readList(String json, TypeReference<List<T>> type) throws IOException {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        return objectMapper.readValue(json, type);
    }
}
